// access 토큰 + 사용자 ID 보관소.
//
// [변경 전] 메모리 전용(let accessToken) → 하드 새로고침마다 토큰이 사라져
//           첫 보호 요청이 401 → 인터셉터가 매번 /api/users/reissue 를 호출했다.
// [변경 후] sessionStorage 에 보관 → 새로고침에도 토큰이 살아남아
//           reissue-on-refresh 가 사라진다.
//
// [추가] userId 도 sessionStorage 에 보관 → client.ts 인터셉터에서
//        authStore 를 직접 import 하면 순환참조가 생기므로,
//        tokenStore 를 통해 userId 를 주입한다.
//
//  - sessionStorage 는 탭이 닫히면 자동 삭제되고 탭 간 공유되지 않아
//    localStorage 보다 노출 창이 작다(메모리 < sessionStorage < localStorage).
//  - refresh 토큰은 여전히 HttpOnly 쿠키이므로 JS 에서 접근하지 않는다.
//  - access 가 sessionStorage 에 노출되므로 백엔드의 access 만료(access-expire-ms)는
//    짧게(예: 15~30분) 유지해 탈취 시 위험 창을 줄이는 것을 권장.
//  - 프라이빗 모드 등 sessionStorage 가 막힌 환경에서는 메모리로 폴백한다.

const TOKEN_KEY = "dh.accessToken";
const USER_ID_KEY = "dh.userId";

// sessionStorage 가 실제로 쓸 수 있는지 1회만 확인 (사파리 프라이빗 등 대비)
const storage: Storage | null = (() => {
  try {
    if (typeof window === "undefined" || !window.sessionStorage) return null;
    const probe = "__dh_probe__";
    window.sessionStorage.setItem(probe, "1");
    window.sessionStorage.removeItem(probe);
    return window.sessionStorage;
  } catch {
    return null;
  }
})();

// sessionStorage 를 못 쓰는 환경에서의 메모리 폴백 (기존과 동일한 동작)
let memoryToken: string | null = null;
let memoryUserId: string | null = null;

export const tokenStore = {
  // ── access token ──────────────────────────────────────────────────────────
  get(): string | null {
    if (storage) {
      try {
        return storage.getItem(TOKEN_KEY);
      } catch {
        return memoryToken;
      }
    }
    return memoryToken;
  },

  set(token: string | null): void {
    memoryToken = token; // 폴백/일관성용 미러
    if (!storage) return;
    try {
      if (token) storage.setItem(TOKEN_KEY, token);
      else storage.removeItem(TOKEN_KEY);
    } catch {
      /* 저장 실패 시 메모리에만 보관 */
    }
  },

  // ── user id ────────────────────────────────────────────────────────────────
  getUserId(): string | null {
    if (storage) {
      try {
        return storage.getItem(USER_ID_KEY);
      } catch {
        return memoryUserId;
      }
    }
    return memoryUserId;
  },

  setUserId(userId: string | null): void {
    memoryUserId = userId; // 폴백/일관성용 미러
    if (!storage) return;
    try {
      if (userId) storage.setItem(USER_ID_KEY, userId);
      else storage.removeItem(USER_ID_KEY);
    } catch {
      /* 저장 실패 시 메모리에만 보관 */
    }
  },

  // ── clear all ──────────────────────────────────────────────────────────────
  clear(): void {
    memoryToken = null;
    memoryUserId = null;
    if (!storage) return;
    try {
      storage.removeItem(TOKEN_KEY);
      storage.removeItem(USER_ID_KEY);
    } catch {
      /* noop */
    }
  }
};
