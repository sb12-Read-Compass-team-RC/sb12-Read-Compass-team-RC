import { useNavigate, useSearchParams } from "react-router-dom";
import clsx from "clsx";
import getImagePath from "@/constants/images.ts";
import Button from "@/components/common/Buttons/Button.tsx";

const ERROR_MESSAGES: Record<string, string> = {
  access_denied: "소셜 로그인 접근이 거부되었습니다.",
  server_error: "서버 오류가 발생했습니다.",
  temporarily_unavailable: "서비스가 일시적으로 이용 불가능합니다.",
  invalid_token: "인증 토큰이 유효하지 않습니다.",
  default: "소셜 로그인에 실패했습니다.",
};

export default function AuthErrorPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const errorCode = searchParams.get("error") ?? "default";
  const errorMessage =
    ERROR_MESSAGES[errorCode] ?? ERROR_MESSAGES["default"];

  return (
    <div
      className={clsx(
        "flex flex-col items-center justify-center text-center px-10",
        "min-h-screen"
      )}
    >
      <img
        src={getImagePath("/icon/ic_exclamation-circle.svg")}
        alt="오류"
        width={64}
        height={64}
        className="mb-6 opacity-40"
      />

      <p className="text-[26px] font-semibold text-gray-800">
        로그인에 실패했어요
        <span className="block font-normal text-[18px] text-gray-400 mt-[6px]">
          {errorMessage}
        </span>
      </p>

      <div className="mt-10 flex flex-col gap-3 items-center">
        <Button
          type="button"
          variant="primary"
          className="min-w-[180px]"
          onClick={() => navigate("/login", { replace: true })}
        >
          다시 로그인하기
        </Button>
        <Button
          type="button"
          variant="secondary"
          className="min-w-[180px]"
          onClick={() => navigate("/", { replace: true })}
        >
          홈으로 돌아가기
        </Button>
      </div>
    </div>
  );
}
