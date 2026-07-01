import { JSX } from "react";

// 백엔드(스프링) 주소. OAuth2 는 전체 페이지 리다이렉트라 프록시가 아닌 절대주소를 쓴다.
const BACKEND_URL =
  import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";

type Provider = "google" | "naver";

function startOAuth(provider: Provider) {
  // 스프링 시큐리티 기본 진입점: /oauth2/authorization/{registrationId}
  window.location.href = `${BACKEND_URL}/oauth2/authorization/${provider}`;
}

interface SocialButtonProps {
  provider: Provider;
  label: string;
  icon: JSX.Element;
  className: string;
}

function SocialButton({ provider, label, icon, className }: SocialButtonProps) {
  return (
    <button
      type="button"
      onClick={() => startOAuth(provider)}
      className={`flex w-full items-center justify-center gap-2 rounded-lg border py-3 text-body2 font-semibold transition-colors ${className}`}
    >
      {icon}
      <span>{label}</span>
    </button>
  );
}

export default function SocialLoginButtons() {
  return (
    <div className="flex flex-col gap-3">
      <div className="my-2 flex items-center gap-3">
        <span className="h-px flex-1 bg-gray-200" />
        <span className="text-body3 text-gray-400">또는 소셜 계정으로</span>
        <span className="h-px flex-1 bg-gray-200" />
      </div>

      <SocialButton
        provider="google"
        label="Google로 계속하기"
        className="border-gray-300 bg-white text-gray-700 hover:bg-gray-50"
        icon={
          <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
            <path
              fill="#4285F4"
              d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.72v2.26h2.92c1.7-1.57 2.68-3.88 2.68-6.62z"
            />
            <path
              fill="#34A853"
              d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.92-2.26c-.8.54-1.84.86-3.04.86-2.34 0-4.32-1.58-5.03-3.7H.96v2.33A9 9 0 0 0 9 18z"
            />
            <path
              fill="#FBBC05"
              d="M3.97 10.72A5.41 5.41 0 0 1 3.68 9c0-.6.1-1.18.29-1.72V4.95H.96A9 9 0 0 0 0 9c0 1.45.35 2.82.96 4.05l3.01-2.33z"
            />
            <path
              fill="#EA4335"
              d="M9 3.58c1.32 0 2.5.45 3.44 1.35l2.58-2.58C13.46.89 11.43 0 9 0A9 9 0 0 0 .96 4.95l3.01 2.33C4.68 5.16 6.66 3.58 9 3.58z"
            />
          </svg>
        }
      />

      <SocialButton
        provider="naver"
        label="네이버로 계속하기"
        className="border-[#03C75A] bg-[#03C75A] text-white hover:brightness-95"
        icon={
          <svg width="16" height="16" viewBox="0 0 20 20" aria-hidden="true">
            <path
              fill="#ffffff"
              d="M13.56 10.7 6.16 0H0v20h6.44V9.3l7.4 10.7H20V0h-6.44z"
            />
          </svg>
        }
      />
    </div>
  );
}
