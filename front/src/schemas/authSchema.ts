import { z } from "zod";

// 백엔드와 동일한 비밀번호 패턴
const passwordRegex =
    /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,20}$/;

// 로그인 스키마
export const loginSchema = z.object({
  email: z
  .string()
  .min(1, "이메일을 입력해주세요")
  .pipe(z.email("유효하지 않은 이메일입니다")),

  password: z
  .string()
  .min(1, "비밀번호를 입력해주세요")
  .regex(
      passwordRegex,
      "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다"
  )
});

// 회원가입 스키마
export const signupSchema = z
.object({
  email: z
  .string()
  .min(1, "이메일을 입력해주세요")
  .pipe(z.email("유효하지 않은 이메일입니다")),

  nickname: z
  .string()
  .min(1, "닉네임을 입력해주세요")
  .min(2, "닉네임은 2자 이상 입력해주세요")
  .max(10, "닉네임은 10자 이하로 입력해주세요"),

  password: z
  .string()
  .min(1, "비밀번호를 입력해주세요")
  .regex(
      passwordRegex,
      "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다"
  ),

  confirmPassword: z
  .string()
  .min(1, "비밀번호 확인을 입력해주세요")
})
.refine((data) => data.password === data.confirmPassword, {
  message: "비밀번호가 일치하지 않습니다",
  path: ["confirmPassword"]
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type SignupFormData = z.infer<typeof signupSchema>;