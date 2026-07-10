import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { tokenStore } from "@/api/tokenStore";
import { useAuthStore } from "@/store/authStore";
import getImagePath from "@/constants/images.ts";

/**
 * 백엔드 OAuth2SuccessHandler 가
 *   {redirect-uri}?access=...&userId=...&nickname=...
 * 형태로 리다이렉트한다.
 *
 * HashRouter 를 쓰는 프로젝트라 location.search 가 해시 앞/뒤 어디든 올 수 있어
 * window.location.href 전체에서 쿼리를 직접 파싱한다.
 */
function parseParamsFromUrl(): URLSearchParams {
  const href = window.location.href;
  const queryIndex = href.indexOf("?");
  if (queryIndex === -1) return new URLSearchParams();
  // 해시(#) 뒤에 쿼리가 붙는 경우까지 커버
  const query = href.substring(queryIndex + 1);
  return new URLSearchParams(query);
}

export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const setOAuthUser = useAuthStore(s => s.setOAuthUser);
  const [error, setError] = useState<string | null>(null);
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const params = parseParamsFromUrl();
    const access = params.get("access");
    const userId = params.get("userId");
    const nickname = params.get("nickname");
    const failed = params.get("error");

    if (failed || !access || !userId) {
      setError("소셜 로그인에 실패했습니다. 다시 시도해주세요.");
      const t = setTimeout(() => navigate("/login", { replace: true }), 1500);
      return () => clearTimeout(t);
    }

    // access 토큰 메모리 저장
    tokenStore.set(access);

    // store 에 사용자 정보 반영
    setOAuthUser({
      id: userId,
      nickname: nickname ?? ""
    });

    // 주소창에 토큰이 남지 않도록 깨끗한 경로로 교체
    navigate("/", { replace: true });
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
