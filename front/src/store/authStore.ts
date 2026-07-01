import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authApi } from "@/api/auth";
import { tokenStore } from "@/api/tokenStore";
import { getRoleFromToken } from "@/utils/authRole";
import type { SignupRequest, LoginRequest, User } from "@/types/auth";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isInitialized: boolean;
  error: string | null;
}

interface AuthActions {
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, nickname: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  // OAuth2 콜백에서 받은 사용자 정보로 로그인 상태 세팅
  setOAuthUser: (user: User) => void;
  // 리프레시 실패(세션 만료) 시 서버 호출 없이 즉시 로그아웃 상태로 초기화
  sessionExpired: () => void;
  // 새로고침 시 refresh 쿠키로 세션 복구
  restore: () => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
  setInitialized: (initialized: boolean) => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
  persist(
    set => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      isInitialized: false,
      error: null,

      login: async (email: string, password: string) => {
        set({ isLoading: true, error: null });
        try {
          const loginData: LoginRequest = { email, password };
          const response = await authApi.login(loginData);

          // userId 를 tokenStore 에 동기화 → client.ts 인터셉터가 헤더에 주입
          tokenStore.setUserId((response as User).id ?? null);

          set({
            user: response as User,
            isAuthenticated: true,
            isLoading: false,
            error: null
          });
        } catch (error) {
          set({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error:
              error instanceof Error ? error.message : "로그인에 실패했습니다."
          });
          throw error;
        }
      },

      signup: async (email: string, nickname: string, password: string) => {
        set({ isLoading: true, error: null });
        try {
          const signupData: SignupRequest = { email, nickname, password };
          await authApi.signup(signupData);

          set({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error: null
          });
        } catch (error) {
          set({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error:
              error instanceof Error
                ? error.message
                : "회원가입에 실패했습니다."
          });
          throw error;
        }
      },

      logout: async () => {
        await authApi.logout();
        tokenStore.clear(); // token + userId 둘 다 삭제
        set({
          user: null,
          isAuthenticated: false,
          isLoading: false,
          error: null
        });
      },

      setOAuthUser: (user: User) => {
        // OAuth2 로그인도 userId 동기화 필요
        tokenStore.setUserId(user.id ?? null);
        // OAuth 리다이렉트에는 role 이 없으므로 access 토큰에서 보강한다.
        const role = user.role ?? getRoleFromToken(tokenStore.get());
        set({
          user: { ...user, role: role ?? undefined },
          isAuthenticated: true,
          isLoading: false,
          error: null
        });
      },

      // 리프레시 토큰이 만료/소실되어 재발급이 실패한 경우.
      // 서버 세션은 이미 죽어있으므로 logout API 를 호출하지 않고,
      // 메모리 토큰과 영구 저장된 로그인 상태(localStorage)만 초기화한다.
      sessionExpired: () => {
        tokenStore.clear(); // token + userId 둘 다 삭제
        set({
          user: null,
          isAuthenticated: false,
          isLoading: false,
          error: null
        });
      },

      restore: async () => {
        // access 토큰이 메모리에 없으면 refresh 쿠키로 복구 시도
        const me = await authApi.restoreSession();
        if (me) {
          // 세션 복구 시에도 userId 동기화
          tokenStore.setUserId(me.id ?? null);
          // /api/users/me 응답(UserDto)에는 role 이 없으므로 access 토큰에서 보강한다.
          const role = me.role ?? getRoleFromToken(tokenStore.get());
          set({
            user: { ...me, role: role ?? undefined },
            isAuthenticated: true
          });
        } else {
          tokenStore.setUserId(null);
          set({ user: null, isAuthenticated: false });
        }
      },

      clearError: () => set({ error: null }),
      setLoading: (loading: boolean) => set({ isLoading: loading }),
      setInitialized: (initialized: boolean) =>
        set({ isInitialized: initialized })
    }),
    {
      name: "auth-storage",
      // access 토큰은 저장하지 않는다(메모리 전용). 사용자 표시 정보만 캐시.
      partialize: state => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated
      }),
      onRehydrateStorage: () => state => {
        if (state) {
          // localStorage 에서 user 가 복원됐으면 tokenStore 에도 userId 동기화
          if (state.user?.id) {
            tokenStore.setUserId(state.user.id);
          }
          state.setInitialized(true);
        }
      }
    }
  )
);
