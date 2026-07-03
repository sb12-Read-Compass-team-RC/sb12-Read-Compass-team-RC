export const BOOKS_ORDERBY = [
  { value: "title", label: "제목순" },
  { value: "publishedDate", label: "출판일순" },
  { value: "rating", label: "평점순" },
  { value: "reviewCount", label: "리뷰순" }
] as const;

export const REVEWS_ORDERBY = [
  { value: "createdAt", label: "시간순" },
  { value: "rating", label: "평점순" }
] as const;

export const SORT_DIRECTION = [
  { value: "DESC", label: "내림차순" },
  { value: "ASC", label: "오름차순" }
] as const;

export const BOOK_CATEGORIES = [
  { value: "", label: "카테고리 선택" },
  { value: "NOVEL", label: "소설" },
  { value: "POETRY_ESSAY", label: "시/에세이" },
  { value: "HUMANITIES", label: "인문" },
  { value: "FAMILY_PARENTING", label: "가정/육아" },
  { value: "COOKING", label: "요리" },
  { value: "HEALTH", label: "건강" },
  { value: "HOBBY_SPORTS", label: "취미/스포츠" },
  { value: "ECONOMY_BUSINESS", label: "경제/경영" },
  { value: "SELF_DEVELOPMENT", label: "자기계발" },
  { value: "POLITICS_SOCIETY", label: "정치/사회" },
  { value: "HISTORY_CULTURE", label: "역사/문화" },
  { value: "RELIGION", label: "종교" },
  { value: "ART_POP_CULTURE", label: "예술/대중문화" },
  { value: "MIDDLE_HIGH_SCHOOL", label: "중/고등참고서" },
  { value: "TECHNOLOGY_ENGINEERING", label: "기술/공학" },
  { value: "FOREIGN_LANGUAGE", label: "외국어" },
  { value: "SCIENCE", label: "과학" },
  { value: "EXAM_JOB", label: "수험서/자격증" },
  { value: "TRAVEL", label: "여행" },
  { value: "COMPUTER_IT", label: "컴퓨터/IT" },
  { value: "MAGAZINE", label: "잡지" },
  { value: "TEEN", label: "청소년" },
  { value: "ELEMENTARY_STUDY", label: "초등참고서" },
  { value: "INFANT", label: "유아" },
  { value: "CHILDREN", label: "어린이" },
  { value: "COMICS", label: "만화" },
  { value: "UNIVERSITY_TEXTBOOK", label: "대학교재" },
  { value: "KOREA_INTRODUCTION", label: "한국소개도서" }
] as const;


export const BOOK_CATEGORY_FILTER_OPTIONS = [
  { value: "", label: "전체 카테고리" },
  ...BOOK_CATEGORIES.filter(category => category.value !== "")
] as const;
