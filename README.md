# Read Compass (RC) (수정중)

**덕후감(Deokhugam)**
도서 이미지 OCR 및 ISBN 매칭 기반 독서 기록 커뮤니티 서비스.

책 읽는 즐거움을 공유하고, 지식과 감상을 나누는 책 덕후들의 커뮤니티 플랫폼입니다.

---

# 1. 프로젝트 개요

## 프로젝트명

**덕후감 (Deokhugam)**

## 소개

사용자는 책 정보를 등록하고 리뷰를 작성하며, 댓글과 좋아요를 통해 독서 경험을 공유할 수 있습니다.

OCR 기반 ISBN 추출 기능을 통해 도서를 쉽게 등록할 수 있으며, 배치 시스템을 통해 인기 도서, 인기 리뷰, 파워 유저 랭킹을 관리합니다.

---

# 2. 주요 기능

## 사용자 관리

* 회원가입
* 로그인
* OAuth2 로그인
* JWT 인증/인가
* 닉네임 수정
* 논리 삭제
* 지연 물리 삭제

---

## 도서 관리

* 도서 등록
* ISBN 중복 방지
* Naver API 기반 ISBN 자동 조회
* OCR 기반 ISBN 추출
* S3 이미지 업로드
* 커서 기반 페이지네이션
* 다중 정렬 지원

---

## 리뷰 관리

* 리뷰 등록/수정/삭제
* 도서당 1개 리뷰 제한
* 좋아요 기능
* likedByMe 지원
* 검색/필터링

---

## 댓글 관리

* 댓글 등록/수정/삭제
* 시간순 페이지네이션

---

## 알림 관리

* 좋아요 알림
* 댓글 알림
* 랭킹 진입 알림
* 읽음 처리
* 만료 알림 자동 삭제

---

## 대시보드 관리

* 인기 도서 랭킹
* 인기 리뷰 랭킹
* 파워 유저 랭킹
* 기간별 통계 (일간/주간/월간/전체)

---

# 3. 팀 정보

## 팀명

**RC (Read Compass)**

---

---

# 5. 협업 도구

| 구분    | 도구       |
| ----- | -------- |
| 소통    | Discord  |
| 형상 관리 | Github   |
| 일정 관리 | Jira     |
| 문서화   | Notion   |
| ERD   | ErdCloud |

---

# 6. 기술 스택

| 구분             | 내용                                               |
| -------------- | ------------------------------------------------ |
| Language       | Java 17                                          |
| Build Tool     | Gradle                                           |
| Framework      | Spring Boot 4.1.0, Spring Security, Spring Batch |
| ORM            | Spring Data JPA, QueryDSL                        |
| Authentication | JWT (Access + Refresh + Rotation), OAuth2        |
| Library        | MapStruct, Lombok, Bean Validation, Swagger      |
| Database       | PostgreSQL (AWS RDS)                             |
| Infra          | ECS, ALB, Route53, ACM, CloudWatch, S3           |
| CI/CD          | GitHub Actions                                   |
| Test           | Postman                                          |
| IDE            | IntelliJ                                         |

---

# 7. 협업 규칙

| 구분      | 내용                                  |
| ------- | ----------------------------------- |
| 브랜치 전략  | Git Flow (`main`, `dev`, `feature`) |
| PR 규칙   | `dev` 브랜치 실행 가능 상태 유지               |
| 코드 컨벤션  | Google Java Style                   |
| 오전 스탠드업 | 매일 09:20                            |
| 오후 컨펌   | 매일 18:10                            |
| 이슈 관리   | Jira                                |
| 프로젝트 구조 | Multi Repo                          |
| 폴더 구조   | Domain-based                        |
| 배포 규칙   | main merge 시 자동 배포 (dev/prod 분리)    |
| 작업 순서   | Entity → DTO → Repository → Service |

---

# 8. 개발 기능 및 담당자

| 기능         | 난이도 | 담당자      |
| ---------- | --- | -------- |
| API 명세서 작성 | -   | 정우진      |
| ERD 설계     | -   | 김성규, 전민지 |
| 프로젝트 기초 구축 | -   | 장현우      |
| 인증 / 인가    | 상   | 장현우      |
| 사용자 관리     | 하   | 김성규      |
| 도서 관리      | 상   | 전민지      |
| 리뷰 관리      | 중상  | 정우진      |
| 댓글 관리      | 중   | 이지예      |
| 알림 관리      | 중   | 이지예      |
| 대시보드 Batch | 최상  | 장현우      |
| CI/CD 구축   | 상   | 장현우      |
| AWS 인프라 구축 | 상   | 장현우      |
| 발표 자료      | -   | 전민지      |

---

# 9. 프로젝트 구조

```text
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┗ com.rc.readcompass
 ┃ ┃   ┣ book
 ┃ ┃   ┣ comments
 ┃ ┃   ┣ common
 ┃ ┃   ┣ config
 ┃ ┃   ┣ exception
 ┃ ┃   ┣ jwt
 ┃ ┃   ┣ notification
 ┃ ┃   ┣ oauth2
 ┃ ┃   ┣ review
 ┃ ┃   ┣ storage
 ┃ ┃   ┣ user
 ┃ ┗ resources
 ┃   ┣ application.yml
 ┃   ┗ static
 ┗ test
```

---

# 10. 실행 방법

## PostgreSQL 생성

```sql
CREATE DATABASE readcompass;
```

---

## IntelliJ Database 연결

Database 탭에서 PostgreSQL 연결 후:

* Database Name: `readcompass`
application-secret.yml 필수.

---

## Batch Server

```text id="batch-server-01"
read-compass-batch
 ┣ dashboard
 ┣ ranking
 ┣ notification-cleanup
 ┣ metrics
 ┗ config
```

---

# 7. 인증 구조

## OAuth2 + JWT Hybrid 구조

외부 인증은 OAuth2로 처리하고 내부 인증은 JWT 기반으로 처리합니다.

```text id="auth-flow-01"
Client
→ OAuth2 Provider
→ Authorization Code
→ Access Token 교환
→ 사용자 정보 조회
→ 회원가입 or 로그인
→ 내부 JWT 발급
→ API 요청
```

---

# 8. JWT 전략

## Access Token

* Header 저장
* 짧은 만료 시간

---

## Refresh Token

* Cookie 저장
* HttpOnly 적용
* 장기 로그인 유지

---

## Refresh Rotation

```text id="rotation-flow-01"
Refresh Token A 사용
→ 검증
→ Access 재발급
→ Refresh Token B 재발급
→ A 폐기
```

장점:

* 재사용 공격 방지
* 탈취 감지 가능
* 보안 강화

---

# 9. Batch 전략

배치 서버는 메인 API와 별도 프로젝트로 운영됩니다.

## 배치 작업

### 인기 도서 집계

매일 실행

공식:

```text id="score-book"
(리뷰 수 × 0.4) + (평점 평균 × 0.6)
```

---

### 인기 리뷰 집계

매일 실행

공식:

```text id="score-review"
(좋아요 수 × 0.3) + (댓글 수 × 0.7)
```

---

### 파워 유저 집계

매일 실행

공식:

```text id="score-user"
(리뷰 인기 점수 × 0.5)
+ (좋아요 참여 수 × 0.2)
+ (댓글 참여 수 × 0.3)
```

---

### 알림 정리

읽은 지 7일 지난 알림 삭제

---

# 10. 인프라 구조 (예정)

```text id="infra-01"
Client
↓
Load Balancer
↓
ECS (Main API)
↓
RDS PostgreSQL

ECS (Batch Server)
↓
CloudWatch

S3
(이미지 저장 / 로그 적재)
```

---

# 11. 로그 관리

MDC 기반 요청 추적.

추가 정보:

* Request ID
* Client IP

적용:

* 로그 메시지
* 응답 헤더

추후:

* 날짜별 로그 S3 적재 예정

---

# 12. 배포 전략

Git Flow 기반.

```text id="deploy-flow-01"
feature
→ dev
→ main
→ GitHub Actions
→ ECS Deploy
```

배포 환경:

* dev
* prod

---

# 14. API 명세서

https://app.notion.com/p/38733f6c8da0803ab6dcea5e458a77f8?p=38833f6c8da0800d96d3d65b29523417&pm=c

---

# 15. ERD

https://www.erdcloud.com/d/zbpMnHHNuQ4ixccFK

---

# 16. 회고록

프로젝트 종료 후 업로드 예정.

---

## OAuth2 로그인(로그인) 방식
- 구글, 카카오 같은 외부 인증 제공자에게 회원가입과 로그인 인증을 맡기고, 인증 결과만 전달받는 방식.

요약: 
- 로그인 시작.
-> 클라이언트가 OAuth2 경로로 이동 (예: /oauth2/authorization/google)
-> 소셜 사이트에서 이메일 조회, 프로필 조회 등의 권한 승인.
-> 소셜 사이트가 우리 서버에게 callback (예: /login/oauth2/code/google?code=abc123)
-> 우리 서버가 받은 code와 key를 키반으로 access token 생성 후 교환.
-> 우리 서버에서 access token으로 사용자 가입 or 확인.
-> 조회 성공시 내부 인증용 access/refresh token토큰을 만들어서 프론트로 전달.

---

## 로그인 유지 방식
- 로그인을 유지하기 위한 방식은 세션(Session) 과 JWT(Json Web Token)가 존재.

### 세션 방식
- 세션 방식은 로그인 성공 후 서버가 로그인 유지를 위한 사용자 상태를 저장.
- 상태 기반.

흐름:
- 사용자가 로그인
- 서버가 세션 생성
- 세션 ID를 브라우저에 저장
- 이후 요청마다 세션 ID 전달
- 서버가 세션 저장소에서 사용자 확인

장점:
- 서버가 강제로 로그아웃 가능
- 토큰 탈취 위험이 상대적으로 적음

단점:
- 로그인이 필요한 작업마다 DB 접근.
- 서버 메모리 사용량 증가
- 서버가 늘어나면 세션 공유 필요 (Redis 등)
- 확장성이 떨어짐

---

### JWT 방식
- JWT는 서버가 로그인 유지를 위한 사용자 상태를 저장하지 않음.

로그인 성공 시:
- Access Token 발급
- Refresh Token 발급

이후 요청:
- 클라이언트가 Access Token 전송
- 서버는 서명 검증만 수행
- 유효하면 인증 완료

장점:
- 서버가 상태를 저장하지 않음 (Stateless)
- 확장성이 좋음
- 서버 간 인증 공유가 쉬움

---

## JWT
- JWT만 쓰면 문제가 있음

예:
- Access Token 만료시간 = 7일
- 토큰이 탈취되면: 공격자가 7일 동안 계속 사용 가능

해결 방법:
- Access Token 만료 시간을 짧게 설정.

장점:
- 탈취되어도 피해 시간이 짧음

문제:
- 30분마다 다시 로그인으로 인해 매우 낮은 사용성.

해결 방법:
- Access Token을 재발급하기 위한 용도인 Refresh Token 생성.

흐름:
- Access Token 만료
- 클라이언트가 Refresh Token 전달
- 서버가 검증
- 새로운 Access Token 발급

장점:
- 사용자는 로그인 상태를 유지 가능.

문제:
- Refresh Token 만료시간 = 14일
- Refresh Token도 탈취되면 공격자가 계속 Access Token을 재발급 받을 수 있음.

해결 방법:
- Refresh Token을 사용할 때마다 새 토큰으로 교체. (Rotation방식)

흐름:
- Refresh Token A 사용
- 서버 검증 성공
- Access Token 발급
- Refresh Token B 새 발급
- 기존 Token A 폐기

장점:
- 재사용 공격 방지
- 보안 강화
- 탈취 감지 가능

---

## JWT 방식을 쓴 이유
- 서버 간 인증 공유 용이성
- OAuth2 연동 편의성
- Access/Refresh 분리로 보안 강화
- Stateless 운영

---

### 서버 간 인증 공유 용이성
- 현재 서비스는 AWS ECS 환경, 메인 프로젝트 외에 배치 프로젝트가 별도로 존재한다. JWT는 서버 간 인증 공유가 단순.

세션 방식이면:
- 각 서버가 동일한 세션 저장소를 공유해야 함
- 추가적으로 Redis 같은 중앙 저장소가 필요함
- 세션 동기화 비용 발생

JWT는:
- 서버가 사용자 상태를 저장하지 않음
- 토큰 자체에 인증 정보 포함
- 어느 ECS 인스턴스가 요청을 받아도 동일하게 검증 가능

---

### OAuth2 연동 편의성
- OAuth2 로그인은 외부 인증 제공자(구글, 카카오)로부터 사용자 신원 확인만 위임받는다.
- 하지만 서비스 내부에서는 별도의 인증 체계가 필요.

OAuth2 로그인 성공 후:
- 소셜 Access Token은 외부 서비스 전용
- 우리 서비스 API 인증에는 부적합

그래서:
- OAuth2로 사용자 인증 완료
- 내부 JWT Access/Refresh Token 발급
- 이후 모든 API 요청은 내부 JWT 기반 인증

---

### Access/Refresh 분리로 보안 강화

현재 인증 흐름은:
- Access Token → Header 저장
- Refresh Token → Cookie 저장

이 구조의 장점:

Access Token (Header)
- API 요청마다 명시적으로 전달-
- 서버가 빠르게 인증 가능
- RESTful 구조에 적합

Refresh Token (Cookie)
- HttpOnly 설정 가능
- JavaScript 접근 차단 가능
- 탈취 위험 감소

즉:
- Access는 요청 효율성
- Refresh는 보안성

---

### Stateless 운영
- 현재 DB는 인증 상태 전체를 저장하지 않고 Refresh Token 정보(블랙리스트/회전 관리 정보)만 관리하면 된다.

세션 방식처럼 모든 로그인 상태를 저장하지 않아:
- DB 부하 감소
- 인증 처리 단순화
