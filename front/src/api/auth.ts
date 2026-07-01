import { apiClient, API_ENDPOINTS } from "./client";
import { tokenStore } from "./tokenStore";
import type {
  SignupRequest,
  SignupResponse,
  LoginRequest,
  LoginResponse,
  User,
  UserNicknameRequest
} from "@/types/auth";

// 인증 API 함수들
export const authApi = {
  // 회원가입
  async signup(userData: SignupRequest): Promise<SignupResponse> {
    try {
      return await apiClient.post<SignupResponse>(
        API_ENDPOINTS.USERS.SIGNUP,
        userData
      );
    } catch (error) {
      console.error("회원가입 API 에러:", error);
      if (error instanceof Error) {
        if (error.message.includes("이미 존재")) {
          throw new Error("이미 존재하는 이메일 또는 닉네임입니다.");
        } else if (error.message.includes("400")) {
          throw new Error("입력값을 확인해주세요.");
        } else if (error.message.includes("500")) {
          throw new Error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
      }
      throw new Error("회원가입에 실패했습니다.");
    }
  },

  // 로그인 — 바디(유저정보) + 헤더(access 토큰)를 함께 처리
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const { data, accessToken } = await apiClient.postWithHeaders<LoginResponse>(
        API_ENDPOINTS.USERS.LOGIN,
        credentials
      );
      // access 토큰을 메모리에 저장
      if (accessToken) {
        tokenStore.set(accessToken);
      }
      return data;
    } catch (error) {
      console.error("로그인 API 에러:", error);
      if (error instanceof Error) {
        if (
          error.message.includes("401") ||
          error.message.includes("비밀번호가 올바르지")
        ) {
          throw new Error("이메일 또는 비밀번호가 불일치합니다.");
        } else if (error.message.includes("400")) {
          throw new Error("이메일 또는 비밀번호를 확인해주세요.");
        } else if (error.message.includes("500")) {
          throw new Error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
      }
      throw new Error("로그인에 실패했습니다.");
    }
  },

  // 로그아웃 — 서버에서 refresh 쿠키/DB 제거
  async logout(): Promise<void> {
    try {
      await apiClient.post(API_ENDPOINTS.USERS.LOGOUT, {});
    } catch (error) {
      console.error("로그아웃 API 에러:", error);
    } finally {
      tokenStore.clear();
    }
  },

  // 새로고침 후 세션 복구 — me 호출 시 access 가 없거나 만료면
  // client 인터셉터가 refresh 쿠키로 자동 재발급(rotation)한 뒤 재시도한다.
  async restoreSession(): Promise<User | null> {
    try {
      return await apiClient.get<User>(API_ENDPOINTS.USERS.ME);
    } catch {
      tokenStore.clear();
      return null;
    }
  },

  // 사용자 프로필 조회
  async getUserProfile(userId: string): Promise<User> {
    try {
      return await apiClient.get<User>(API_ENDPOINTS.USERS.PROFILE(userId));
    } catch (error) {
      console.error("사용자 프로필 조회 API 에러:", error);
      throw new Error("사용자 정보를 가져오는데 실패했습니다.");
    }
  }
};

// 프로필 수정
export const patchUserProfile = async (userId: string, data: string) => {
  try {
    return await apiClient.patch<UserNicknameRequest>(
      API_ENDPOINTS.USERS.PROFILE(userId),
      { nickname: data }
    );
  } catch (error) {
    console.error("사용자 프로필 수정 API 에러:", error);
    throw new Error("사용자 정보를 수정하는데 실패했습니다.");
  }
};

// 회원 탈퇴
export const deleteUser = async (userId: string) => {
  try {
    await apiClient.delete(API_ENDPOINTS.USERS.PROFILE(userId));
  } catch (error) {
    console.error("사용자 탈퇴 API 에러:", error);
    throw new Error("사용자의 탈퇴를 실패했습니다.");
  }
};
