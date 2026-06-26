package com.rc.readcompass.book.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookCategory {
  NOVEL("소설"),
  POETRY_ESSAY("시/에세이"),
  HUMANITIES("인문"),
  FAMILY_PARENTING("가정/육아"),
  COOKING("요리"),
  HEALTH("건강"),
  HOBBY_SPORTS("취미/스포츠"),
  ECONOMY_BUSINESS("경제/경영"),
  SELF_DEVELOPMENT("자기계발"),
  POLITICS_SOCIETY("정치/사회"),
  HISTORY_CULTURE("역사/문화"),
  RELIGION("종교"),
  ART_POP_CULTURE("예술/대중문화"),
  MIDDLE_HIGH_SCHOOL("중/고등참고서"),
  TECHNOLOGY_ENGINEERING("기술/공학"),
  FOREIGN_LANGUAGE("외국어"),
  SCIENCE("과학"),
  EXAM_JOB("수험서/자격증"),
  TRAVEL("여행"),
  COMPUTER_IT("컴퓨터/IT"),
  MAGAZINE("잡지"),
  TEEN("청소년"),
  ELEMENTARY_STUDY("초등참고서"),
  INFANT("유아"),
  CHILDREN("어린이"),
  COMICS("만화"),
  UNIVERSITY_TEXTBOOK("대학교재"),
  KOREA_INTRODUCTION("한국소개도서");

  private final String label;

  public static BookCategory fromKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }

    String normalizedKeyword = keyword.trim().toLowerCase();

    for (BookCategory category : values()) {
      if (category.name().toLowerCase().contains(normalizedKeyword)
          || category.label.toLowerCase().contains(normalizedKeyword)) {
        return category;
      }
    }

    return null;
  }
}
