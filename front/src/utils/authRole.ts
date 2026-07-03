// 클라이언트 권한(role) 판별 유틸.
//
// 주의: 이것은 "보안"이 아니라 "버튼 노출 편의"용입니다.
// 실제 권한 강제는 백엔드(@PreAuthorize + 서비스 검증)에서 이뤄지며,
// 토큰은 서명되어 있어 role 을 위조해도 서버가 거부합니다.
//
// role 은 access 토큰(JWT) 안에 이미 들어있으므로, 로그인/새로고침/OAuth
// 어떤 경로로 들어와도 토큰 하나만 있으면 동일하게 판별할 수 있습니다.

// JWT payload 를 디코드해 role 클레임을 반환한다. (예: "ROLE_ADMIN")
export function getRoleFromToken(token: string | null | undefined): string | null {
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    // base64url -> base64
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map(c => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    const claims = JSON.parse(json);
    return claims.role ?? null;
  } catch {
    return null;
  }
}

// role 문자열이 관리자인지 판별. "ROLE_ADMIN" / "ADMIN" 둘 다 허용.
export function isAdmin(role: string | null | undefined): boolean {
  if (!role) return false;
  return role === "ROLE_ADMIN" || role === "ADMIN";
}
