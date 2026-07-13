# 덕후감 (Deokhugam)

---

> **도서 이미지 OCR 및 ISBN 매칭 기반 독서 기록 커뮤니티 서비스**

책 읽는 즐거움을 공유하는 책 덕후들의 커뮤니티 플랫폼입니다.  
사용자는 도서를 등록하고 리뷰를 작성하며, 댓글과 좋아요를 통해 평가와 감상을 나누며 독서 경험을 공유할 수 있습니다. **OCR 기반 ISBN 추출**로 도서를 쉽게 등록할 수 있으며, 별도의 **배치 서버**가 인기 도서 · 인기 리뷰 · 파워 유저 랭킹을 매일 집계합니다.

<br/>

**프로젝트 기간:** `2026.06.22 ~ 2026.07.13`
<br/>

## 👥 팀 소개 : Read-compass

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/gusdn6763">
        <img src="https://github.com/gusdn6763.png" width="100px;" alt="장현우"/>
      </a>
      <br/>
      <sub><b>[팀장] 장현우</b></sub>
      <br/>
      <a href="https://github.com/gusdn6763">
        <img src="https://img.shields.io/badge/GitHub-gusdn6763-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/jeon-minji">
        <img src="https://github.com/jeon-minji.png" width="100px;" alt="전민지"/>
      </a>
      <br/>
      <sub><b>전민지</b></sub>
      <br/>
      <a href="https://github.com/jeon-minji">
        <img src="https://img.shields.io/badge/GitHub-jeon--minji-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/zinzin68">
        <img src="https://github.com/zinzin68.png" width="100;" alt="정우진"/>
      </a>
      <br/>
      <sub><b>정우진</b></sub>
      <br/>
      <a href="https://github.com/zinzin68">
        <img src="https://img.shields.io/badge/GitHub-zinzin68-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/yecoder514">
        <img src="https://github.com/yecoder514.png" width="100px;" alt="이지예"/>
      </a>
      <br/>
      <sub><b>이지예</b></sub>
      <br/>
      <a href="https://github.com/yecoder514">
        <img src="https://img.shields.io/badge/GitHub-yecoder514-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/ksg-97">
        <img src="https://github.com/ksg-97.png" width="100;" alt="김성규"/>
      </a>
      <br/>
      <sub><b>김성규</b></sub>
      <br/>
      <a href="https://github.com/ksg-97ksg-97">
        <img src="https://img.shields.io/badge/GitHub-ksg--97-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
  </tr>
</table>
</br>

[🔗 Read-Compass 팀 페이지 ](https://app.notion.com/p/Read-Compass-2e25ae3ee5a58346b5a601f9b9f1182f)

## 🚩 주요 기능

| 도메인 | 기능 |
| --- | --- |
| 사용자 | 회원가입, 로그인, **OAuth2 소셜 로그인**, **JWT 인증/인가**, 닉네임 수정, 논리 삭제 + 지연 물리 삭제 |
| 도서 | 도서 등록/수정/삭제, ISBN 중복 방지, **Naver API 기반 도서 정보 자동 조회**, **OCR 기반 ISBN 추출**, S3 이미지 업로드, 커서 페이지네이션 |
| 리뷰 | 리뷰 CRUD (도서당 1개 제한), 좋아요, likedByMe, 키워드 검색/필터링 |
| 댓글 | 댓글 CRUD, 시간순 커서 페이지네이션 |
| 알림 | 좋아요/댓글/랭킹 진입 알림, 읽음 처리, 만료 알림 자동 삭제 |
| 대시보드 | 기간별(일간/주간/월간/역대) 인기 도서 · 인기 리뷰 · 파워 유저 랭킹 조회|

## 👥 팀원별 구현 기능 상세

| 이름 | 담당 영역 | 
|---|---|
| 장현우 | 기초 프로젝트 구축, 인증/인가, CI/CD, 인프라, 배치, 대시보드|
| 전민지 | 도서 관리, ERD, 설계, Open API 연동 | 
| 정우진 | 리뷰 관리, API 명세서,  발표 자료 | 
| 이지예 | 댓글 관리, 알림 관리 | 
| 김성규 | 사용자 관리, ERD 설계 |

\* 관련 기능 일부 추가 구현

<br/>

## 🛠️ 기술 스택

### Language & Framework
![Java 17](https://img.shields.io/badge/Java_17-ED8B00?style=flat&logo=openjdk&logoColor=white)  
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.1.0-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=flat&logo=spring&logoColor=white)
<br/>

### Database & ORM
![PostgreSQL (AWS RDS)](https://img.shields.io/badge/PostgreSQL_(AWS_RDS)-4169E1?style=flat&logo=postgresql&logoColor=white)  
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-007ACC?style=flat&logo=target&logoColor=white)
<br/>

### Auth & Security
![JWT](https://img.shields.io/badge/JWT_(_Access_/_Refresh_/_Rotation_)-000000?style=flat&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-3B82F6?style=flat&logo=openid&logoColor=white)
<br/>

### Infrastructure
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![AWS ECS](https://img.shields.io/badge/AWS_ECS-FF9900?style=flat&logo=amazonelasticcontainer-service&logoColor=white)  
![AWS ALB](https://img.shields.io/badge/AWS_ALB-FF9900?style=flat&logo=amazonwebservices&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat&logo=amazons3&logoColor=white)
![AWS CloudWatch](https://img.shields.io/badge/AWS_CloudWatch-FF4F8B?style=flat&logo=amazoncloudwatch&logoColor=white)
<br/>

### CI/CD
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat&logo=githubactions&logoColor=white)
<br/>

### Libraries & Utilities
![MapStruct](https://img.shields.io/badge/MapStruct-EF4444?style=flat&logo=probot&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC262E?style=flat&logo=hotjar&logoColor=white)
![Bean Validation](https://img.shields.io/badge/Bean_Validation-6DB33F?style=flat&logo=hibernate&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=swagger&logoColor=black)

<br/>

## 📁 프로젝트 구조

> 메인 API 서버와 배치 서버는 별도의 프로젝트(Multi Repo)로 운영됩니다.

### 🌐 메인 API 서버
```
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

--- 

### ⚙️ 배치 서버
```
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
  - 정합성 보완 : COUNT 컬럼 값 계산 후 업데이트
</br>

<br/>

## 🔐 인증 구조

### 세션 vs JWT

로그인 상태를 유지하는 방식에는 크게 세션과 JWT가 있습니다.

&emsp;**세션**은 로그인 성공 시 서버가 사용자 상태를 저장하고, 클라이언트는 세션 ID만 들고 다니는 방식입니다. 서버가 강제 로그아웃을 시킬 수 있고 탈취에 상대적으로 안전하지만, 요청마다 세션 저장소 조회가 필요하고 서버가 여러 대로 늘어나면 Redis 같은 중앙 세션 저장소를 공유해야 해서 확장성이 떨어집니다.

&emsp;**JWT**는 인증 정보를 토큰 자체에 담아 클라이언트가 들고 다니고, 서버는 서명 검증만 수행하는 무상태(Stateless) 방식입니다. 서버가 상태를 저장하지 않으므로 어느 서버 인스턴스가 요청을 받아도 동일하게 검증할 수 있어 확장에 유리합니다.

---

### JWT + Refresh + Rotation을 선택한 이유

**1. ECS 다중 서버 환경에서의 확장성**  
<span style="color: transparent;">__</span>이 서비스는 AWS ECS에서 메인 API 서버와 배치 서버가 분리되어 운영됩니다. 세션 방식이라면 서버 간 세션 저장소 공유(Redis 등)와 동기화 비용이 필요하지만, JWT는 토큰 자체에 인증 정보가 있어 어떤 인스턴스든 동일하게 검증할 수 있습니다.

**2. OAuth2 연동 편의성**  
<span style="color: transparent;">__</span>소셜 로그인으로 받은 Access Token은 외부 서비스 전용이라 내부 API 인증에는 쓸 수 없습니다. OAuth2로 신원 확인을 마친 뒤 내부 JWT를 발급하는 하이브리드 구조로, 소셜/일반 로그인 이후의 인증 흐름을 하나로 통일했습니다.

**3. Access/Refresh 분리로 보안과 사용성 확보**  
<span style="color: transparent;">__</span>JWT만 쓰면 토큰 탈취 시 만료 전까지 계속 악용될 수 있습니다. 그래서 Access Token은 만료를 짧게 잡아 헤더로 전달하고(탈취돼도 피해 시간 최소화), 재로그인 없이 이를 재발급하기 위한 Refresh Token은 HttpOnly 쿠키에 저장해 JavaScript 접근을 차단했습니다.

**4. Refresh Rotation으로 탈취 대응**  
<span style="color: transparent;">__</span>Refresh Token마저 탈취되면 공격자가 계속 Access Token을 재발급받을 수 있습니다. 이를 막기 위해 Refresh Token을 사용할 때마다 새 토큰으로 교체하고 기존 토큰은 폐기합니다. 폐기된 토큰이 다시 사용되면 탈취를 감지할 수 있습니다.

```text
Refresh Token A 사용
         ↓
        검증
         ↓
   Access 재발급
         ↓
Refresh Token B 발급
         ↓
       A 폐기
```

<br/>

## ▶️ 프로젝트 실행 방법

1. PostgreSQL 데이터베이스 생성

```sql
CREATE DATABASE readcompass;
```

2. `application-secret.yml` 작성 (DB 접속 정보, JWT 시크릿, OAuth2 클라이언트 키, Naver/OCR API 키, S3 설정)

3. 실행

```bash
./gradlew bootRun
```

<br/>

## 🖥️ 구현 홈페이지
[🔗 덕후감 - 구현 홈페이지](https://read-compass.com/)

</br>

## 📝 프로젝트 자료
[🔗 덕후감 - 프로젝트 자료](https://drive.google.com/file/d/1K_sbTyseOiCOCsybHNFPWQCplpELUVV0/view?usp=drive_link)  
[📄 덕후감 - API 명세](https://app.notion.com/p/38733f6c8da0803ab6dcea5e458a77f8?p=38833f6c8da0800d96d3d65b29523417&pm=c)  
[🗺️ 덕후감 - ERD](https://www.erdcloud.com/d/zbpMnHHNuQ4ixccFK)
