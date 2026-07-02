import NavBar from "@/components/ui/NavBar";
import Tooltip from "@/components/ui/Tooltip";
import Footer from "@/components/ui/Footer";

import clsx from "clsx";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useAuthStore } from "@/store/authStore";


export default function ClientLayout({
}) {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const sessionExpired = useAuthStore(state => state.sessionExpired);
  const restore = useAuthStore(state => state.restore);
  const setInitialized = useAuthStore(state => state.setInitialized);

  // 앱 시작(및 새로고침) 시 서버에 세션이 살아있는지 검증한다.
  // 검증이 끝난 뒤에야 초기화 완료(setInitialized)로 표시하므로,
  // 저장된 localStorage 값만으로 로그인 상태가 유지되는 문제를 막는다.
  useEffect(() => {
    let active = true;
    (async () => {
      try {
        await restore();
      } finally {
        if (active) setInitialized(true);
      }
    })();
    return () => {
      active = false;
    };
  }, [restore, setInitialized]);

  // 리프레시 토큰 만료/소실로 재발급이 실패하면(client.ts 인터셉터가 발생시키는
  // "auth:expired" 이벤트) 로그인 상태를 초기화하고 로그인 페이지로 보낸다.
  useEffect(() => {
    const handleExpired = () => {
      sessionExpired();
      if (
        !pathname.includes("/login") &&
        !pathname.includes("/signup")
      ) {
        navigate("/login");
      }
    };

    window.addEventListener("auth:expired", handleExpired);
    return () => window.removeEventListener("auth:expired", handleExpired);
  }, [navigate, pathname, sessionExpired]);

  const hideNavigation =
    pathname.includes("/login")  || pathname.includes("/signup");

  const hideFooter =
    pathname.includes("/login") ||
    pathname.includes("/signup") ||
    pathname.includes("/books/add") ||
    pathname.includes("/books/") ||
    pathname.includes("/reviews/");

  return (
    <>
      {!hideNavigation && <NavBar />}
      <div
        className={clsx(
          !hideNavigation &&
            "min-h-[calc(100vh-139px)] mt-[67px] px-4 max-w-[1200px] mx-auto"
        )}
      >
        <Outlet/>
      </div>
      {!hideFooter && <Footer />}
      <Tooltip />
    </>
  );
}
