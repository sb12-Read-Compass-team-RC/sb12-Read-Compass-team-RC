import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "@/api/auth";
import { useAuthStore } from "@/store/authStore";
import getImagePath from "@/constants/images.ts";

/**
 * 백엔드 OAuth2LoginSuccessHandler 는 refresh 토큰을 HttpOnly 쿠키로만 심고
 * 파라미터 없이 이 페이지로 리다이렉트한다. (access 를 더 이상 URL 에 싣지 않음)
 *
 * 여기서 POST /api/users/reissue 를 호출해
 *   - access 토큰   : Authorization 응답 헤더
 *   - 유저 정보      : 응답 바디 (id / nickname / role)
 * 를 수령한다. → 일반 로그인과 동일하게 "access 는 항상 헤더로" 규약이 통일된다.
 *
 * 로그인 실패 시에는 OAuth2LoginFailureHandler 가 별도의 실패 URL 로 보내므로,
 * 이 페이지에서 reissue 가 실패하는 경우(쿠키 없음/만료 등)는 로그인으로 돌려보낸다.
 */
export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const setOAuthUser = useAuthStore(s => s.setOAuthUser);
  const [error, setError] = useState<string | null>(null);
  const handled = useRef(false);

  useEffect(() => {
    // StrictMode 이중 실행 방지 — reissue 는 rotation 이라 두 번 호출하면
    // 첫 호출의 refresh 가 무효화되어 두 번째가 401 이 된다.
    if (handled.current) return;
    handled.current = true;

    let timer: ReturnType<typeof setTimeout> | undefined;

    authApi
      .completeOAuthLogin()
      .then(user => {
        // store 에 사용자 정보 반영 (userId 동기화는 setOAuthUser 내부에서 처리)
        setOAuthUser({
          id: user.id,
          nickname: user.nickname ?? "",
          role: user.role
        });
        navigate("/", { replace: true });
      })
      .catch(e => {
        console.error("OAuth 콜백 처리 실패:", e);
        setError("소셜 로그인에 실패했습니다. 다시 시도해주세요.");
        timer = setTimeout(() => navigate("/login", { replace: true }), 1500);
      });

    return () => {
      if (timer) clearTimeout(timer);
    };
  }, [navigate, setOAuthUser]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      {error ? (
        <p className="text-body2 font-medium text-red-500">{error}</p>
      ) : (
        <>
          <img
            src={getImagePath("/common/buttonLoader.gif")}
            alt="로그인 처리 중"
            width={40}
            height={40}
          />
          <p className="text-body2 font-medium text-gray-500">
            로그인 처리 중입니다...
          </p>
        </>
      )}
    </div>
  );
}
