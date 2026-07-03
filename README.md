# 덕후감 (Deokhugam) — Read Compass

도서 이미지 OCR 및 ISBN 매칭 기반 독서 기록 커뮤니티 서비스

책 읽는 즐거움을 공유하고, 지식과 감상을 나누는 책 덕후들의 커뮤니티 플랫폼입니다.
사용자는 도서를 등록하고 리뷰를 작성하며, 댓글과 좋아요를 통해 독서 경험을 공유할 수 있습니다.
OCR 기반 ISBN 추출로 도서를 쉽게 등록할 수 있으며, 별도의 배치 서버가 인기 도서 · 인기 리뷰 · 파워 유저 랭킹을 매일 집계합니다.

---

## 주요 기능

| 도메인 | 기능 |
| --- | --- |
| 사용자 | 회원가입, 로그인, OAuth2 소셜 로그인, JWT 인증/인가, 닉네임 수정, 논리 삭제 + 지연 물리 삭제 |
| 도서 | 도서 등록/수정/삭제, ISBN 중복 방지, Naver API 기반 도서 정보 자동 조회, OCR 기반 ISBN 추출, S3 이미지 업로드, 커서 페이지네이션 |
| 리뷰 | 리뷰 CRUD (도서당 1개 제한), 좋아요, likedByMe, 키워드 검색/필터링 |
| 댓글 | 댓글 CRUD, 시간순 커서 페이지네이션 |
| 알림 | 좋아요/댓글/랭킹 진입 알림, 읽음 처리, 만료 알림 자동 삭제 |
| 대시보드 | 기간별(일간/주간/월간/역대) 인기 도서 · 인기 리뷰 · 파워 유저 랭킹 |

---

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.1.0, Spring Security, Spring Batch |
| ORM | Spring Data JPA, QueryDSL |
| Auth | JWT (Access + Refresh + Rotation), OAuth2 |
| Database | PostgreSQL (AWS RDS) |
| Infra | AWS ECS, ALB, S3, CloudWatch |
| CI/CD | GitHub Actions |
| Library | MapStruct, Lombok, Bean Validation, Swagger |

---

## 프로젝트 구조

메인 API 서버와 배치 서버는 별도 프로젝트(Multi Repo)로 운영됩니다.

### 메인 API 서버

```text
com.rc.readcompass
 ┣ book           # 도서 관리 (Naver API, OCR, S3 업로드)
 ┣ review         # 리뷰, 좋아요, 인기 리뷰 조회
 ┣ comments       # 댓글
 ┣ notification   # 알림
 ┣ user           # 사용자, 파워 유저 조회
 ┣ jwt            # JWT 발급/검증 필터, Refresh Rotation
 ┣ oauth2         # OAuth2 소셜 로그인
 ┣ storage        # 파일 저장 (S3)
 ┣ config         # Security, JPA, Web 설정
 ┣ exception      # 커스텀 예외, 전역 예외 처리
 ┗ common         # 공통 모듈 (커서 페이지네이션 등)
```

### 배치 서버

```text
com.rc.readcompassbatch
 ┣ batch          # Job/Step 구성 (config, tasklet, listener)
 ┣ scheduler      # 매일 배치 실행 스케줄러
 ┣ metrics        # 커스텀 메트릭 (Spring Actuator 모니터링)
 ┣ domain         # 랭킹 엔티티
 ┣ repository     # 집계 쿼리
 ┣ service
 ┗ common
```

배치 작업은 매일 실행되며 다음을 집계합니다.

- 인기 도서: `(기간 내 리뷰 수 × 0.4) + (평점 평균 × 0.6)`
- 인기 리뷰: `(기간 내 좋아요 수 × 0.3) + (댓글 수 × 0.7)`
- 파워 유저: `(작성 리뷰의 인기 점수 × 0.5) + (좋아요 참여 × 0.2) + (댓글 참여 × 0.3)`
- 알림 정리: 읽은 지 7일 지난 알림 삭제

---

## 인증 구조

### 세션 vs JWT

로그인 상태를 유지하는 방식에는 크게 세션과 JWT가 있습니다.

**세션**은 로그인 성공 시 서버가 사용자 상태를 저장하고, 클라이언트는 세션 ID만 들고 다니는 방식입니다. 서버가 강제 로그아웃을 시킬 수 있고 탈취에 상대적으로 안전하지만, 요청마다 세션 저장소 조회가 필요하고 서버가 여러 대로 늘어나면 Redis 같은 중앙 세션 저장소를 공유해야 해서 확장성이 떨어집니다.

**JWT**는 인증 정보를 토큰 자체에 담아 클라이언트가 들고 다니고, 서버는 서명 검증만 수행하는 무상태(Stateless) 방식입니다. 서버가 상태를 저장하지 않으므로 어느 서버 인스턴스가 요청을 받아도 동일하게 검증할 수 있어 확장에 유리합니다.

### JWT + Refresh + Rotation을 선택한 이유

**1. ECS 다중 서버 환경에서의 확장성** — 이 서비스는 AWS ECS에서 메인 API 서버와 배치 서버가 분리되어 운영됩니다. 세션 방식이라면 서버 간 세션 저장소 공유(Redis 등)와 동기화 비용이 필요하지만, JWT는 토큰 자체에 인증 정보가 있어 어떤 인스턴스든 동일하게 검증할 수 있습니다.

**2. OAuth2 연동 편의성** — 소셜 로그인으로 받은 Access Token은 외부 서비스 전용이라 내부 API 인증에는 쓸 수 없습니다. OAuth2로 신원 확인을 마친 뒤 내부 JWT를 발급하는 하이브리드 구조로, 소셜/일반 로그인 이후의 인증 흐름을 하나로 통일했습니다.

**3. Access/Refresh 분리로 보안과 사용성 확보** — JWT만 쓰면 토큰 탈취 시 만료 전까지 계속 악용될 수 있습니다. 그래서 Access Token은 만료를 짧게 잡아 헤더로 전달하고(탈취돼도 피해 시간 최소화), 재로그인 없이 이를 재발급하기 위한 Refresh Token은 HttpOnly 쿠키에 저장해 JavaScript 접근을 차단했습니다.

**4. Refresh Rotation으로 탈취 대응** — Refresh Token마저 탈취되면 공격자가 계속 Access Token을 재발급받을 수 있습니다. 이를 막기 위해 Refresh Token을 사용할 때마다 새 토큰으로 교체하고 기존 토큰은 폐기합니다. 폐기된 토큰이 다시 사용되면 탈취를 감지할 수 있습니다.

```text
Refresh Token A 사용 → 검증 → Access 재발급 → Refresh Token B 발급 → A 폐기
```

---

## 실행 방법

1. PostgreSQL 데이터베이스 생성

```sql
CREATE DATABASE readcompass;
```

2. `application-secret.yml` 작성 (DB 접속 정보, JWT 시크릿, OAuth2 클라이언트 키, Naver/OCR API 키, S3 설정)

3. 실행

```bash
./gradlew bootRun
```

---

## 문서

- [API 명세서](https://app.notion.com/p/38733f6c8da0803ab6dcea5e458a77f8?p=38833f6c8da0800d96d3d65b29523417&pm=c)
- [ERD](https://www.erdcloud.com/d/zbpMnHHNuQ4ixccFK)

---

## 팀 정보

**팀명: RC (Read Compass)**

| 기능 | 담당자 |
| --- | --- |
| 인증/인가, 프로젝트 기초 구축, Batch, CI/CD, AWS 인프라 | 장현우 |
| 도서 관리, 발표 자료 | 전민지 |
| 리뷰 관리, API 명세서 | 정우진 |
| 댓글 관리, 알림 관리 | 이지예 |
| 사용자 관리, ERD 설계 | 김성규 |
