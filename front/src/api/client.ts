import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig
} from "axios";
import { tokenStore } from "./tokenStore";

// 모든 환경에서 Vite 프록시(/api -> localhost:8080) 사용
const API_BASE_URL = "";

interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
  skipInterceptor?: boolean;
}

/**
 * 응답 헤더의 Authorization 값에서 access 토큰을 꺼낸다.
 *   "Bearer eyJhbGci..."  ->  "eyJhbGci..."
 * 혹시 "Bearer " 접두사가 없으면 값 그대로 사용(하위 호환).
 * axios 는 응답 헤더 키를 소문자로 보관하므로 호출부에서 headers["authorization"] 로 읽는다.
 */
function extractBearer(value: unknown): string | null {
  if (typeof value !== "string" || value.length === 0) return null;
  return value.startsWith("Bearer ") ? value.slice(7) : value;
}

class ApiClient {
  private axiosInstance: AxiosInstance;

  // 동시 401 발생 시 refresh 를 한 번만 호출하기 위한 잠금
  private isRefreshing = false;
  private refreshWaiters: Array<(token: string | null) => void> = [];

  constructor(baseURL: string) {
    this.axiosInstance = axios.create({
      baseURL,
      headers: { "Content-Type": "application/json" },
      timeout: 10000,
      withCredentials: true // refresh 쿠키 전송을 위해 필수
    });

    // ===== 요청 인터셉터: access 토큰 + User-ID 헤더 첨부 =====
    this.axiosInstance.interceptors.request.use(
      config => {
        const token = tokenStore.get();
        if (token) {
          config.headers["Authorization"] = `Bearer ${token}`;
        }

        // Deokhugam-Request-User-ID 헤더 주입
        // authStore 를 직접 import 하면 순환참조가 발생하므로
        // tokenStore 를 통해 userId 를 읽는다.
        const userId = tokenStore.getUserId();
        if (userId) {
          config.headers["Deokhugam-Request-User-ID"] = userId;
        }

        return config;
      },
      error => Promise.reject(error)
    );

    // ===== 응답 인터셉터: 401 시 refresh rotation 후 재시도 =====
    this.axiosInstance.interceptors.response.use(
      (response: AxiosResponse) => response,
      async error => {
        const originalRequest = error.config as RetryableConfig;

        const status = error.response?.status;
        const isAuthEndpoint =
          originalRequest?.url?.includes("/api/users/reissue") ||
          originalRequest?.url?.includes("/api/users/login");

        // 401 이고, 아직 재시도 안 했고, refresh/login 자체가 아닐 때만 재발급 시도
        if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
          originalRequest._retry = true;

          // 이미 다른 요청이 재발급 중이면 끝날 때까지 대기
          if (this.isRefreshing) {
            const newToken = await new Promise<string | null>(resolve => {
              this.refreshWaiters.push(resolve);
            });
            if (newToken) {
              originalRequest.headers["Authorization"] = `Bearer ${newToken}`;
              return this.axiosInstance(originalRequest);
            }
            return Promise.reject(error);
          }

          this.isRefreshing = true;
          try {
            const newToken = await this.refreshAccessToken();
            this.isRefreshing = false;
            this.refreshWaiters.forEach(resolve => resolve(newToken));
            this.refreshWaiters = [];

            if (newToken) {
              originalRequest.headers["Authorization"] = `Bearer ${newToken}`;
              return this.axiosInstance(originalRequest);
            }
          } catch (refreshError) {
            this.isRefreshing = false;
            this.refreshWaiters.forEach(resolve => resolve(null));
            this.refreshWaiters = [];
            tokenStore.clear(); // token + userId 둘 다 지워짐
            // 세션 만료 → 앱 전역에서 후처리 (로그아웃 등)
            window.dispatchEvent(new CustomEvent("auth:expired"));
            return Promise.reject(refreshError);
          }
        }

        // 일반 에러는 AxiosError 형태를 유지해서 호출부에서 status/codeName/message를 확인할 수 있게 한다.
        return Promise.reject(error);
      }
    );
  }

  // refresh 쿠키로 새 access 토큰 발급 (rotation: 서버가 새 refresh 쿠키도 내려줌)
  // 백엔드 ReissueController 경로(/api/users/reissue)에 맞춘다.
  private async refreshAccessToken(): Promise<string | null> {
    const response = await axios.post(
      API_BASE_URL + "/api/users/reissue",
      {},
      { withCredentials: true }
    );
    const newAccess = extractBearer(response.headers["authorization"]);
    if (newAccess) {
      tokenStore.set(newAccess);
      return newAccess;
    }
    return null;
  }

  async get<T>(
    endpoint: string,
    config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      const response = await axios.get<T>(
        this.axiosInstance.defaults.baseURL + endpoint,
        { ...config, withCredentials: true }
      );
      return response.data;
    }
    const response = await this.axiosInstance.get<T>(endpoint, config);
    return response.data;
  }

  async post<T, D = unknown>(
    endpoint: string,
    data: D,
    config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      const response = await axios.post<T>(
        this.axiosInstance.defaults.baseURL + endpoint,
        data,
        { ...config, withCredentials: true }
      );
      return response.data;
    }
    const response = await this.axiosInstance.post<T>(endpoint, data, config);
    return response.data;
  }

  // 로그인 전용: 응답 헤더(Authorization: Bearer)의 access 토큰까지 함께 반환
  async postWithHeaders<T, D = unknown>(
    endpoint: string,
    data: D
  ): Promise<{ data: T; accessToken: string | null }> {
    const response = await this.axiosInstance.post<T>(endpoint, data);
    const accessToken = extractBearer(response.headers["authorization"]);
    return { data: response.data, accessToken };
  }

  async patch<T, D = unknown>(
    endpoint: string,
    data: D,
    config?: AxiosRequestConfig & { skipInterceptor?: boolean }
  ): Promise<T> {
    if (config?.skipInterceptor) {
      const response = await axios.patch<T>(
        this.axiosInstance.defaults.baseURL + endpoint,
        data,
        { ...config, withCredentials: true }
      );
      return response.data;
    }
    const response = await this.axiosInstance.patch<T>(endpoint, data, config);
    return response.data;
  }

  async delete<T>(endpoint: string): Promise<T> {
    const response = await this.axiosInstance.delete<T>(endpoint);
    return response.data;
  }
}

export const apiClient = new ApiClient(API_BASE_URL);

export const API_ENDPOINTS = {
  USERS: {
    SIGNUP: "/api/users",
    LOGIN: "/api/users/login",
    LOGOUT: "/api/users/logout",
    ME: "/api/users/me",
    REFRESH: "/api/users/reissue",
    PROFILE: (userId: string) => `/api/users/${userId}`
  },
  OAUTH2: {
    GOOGLE: "/oauth2/authorization/google",
    NAVER: "/oauth2/authorization/naver"
  },
  BOOKS: {
    LIST: "/api/books",
    DETAIL: "/api/books/{id}",
    CREATE: "/api/books",
    DELETE: "/api/books/{id}"
  },
  REVIEWS: {
    LIST: "/api/reviews",
    DETAIL: "/api/reviews/{id}",
    CREATE: "/api/reviews",
    DELETE: "/api/reviews/{id}"
  }
} as const;
