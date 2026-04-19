# 🥃 WhiskyNote — 위스키 테이스팅 노트 & AI 취향 분석 API

위스키 시음 기록을 저장하고, AI가 노트를 분석해 개인 취향 키워드를 추출·누적하는 백엔드 API 서버입니다.
동시성 제어, 비동기 처리, 캐싱, 동적 쿼리 등 실무 핵심 기술을 단계별로 적용했습니다.

---

## 📌 주요 기술 포인트

| 기술 | 적용 내용 | 수치 |
|------|---------|------|
| **낙관적 락 + 재시도** | 동시 분석 요청 시 Lost Update 방지 (`@Version` + 최대 10회 재시도) | 동시성 테스트 통과 |
| **병렬 처리** | 키워드 저장 `CompletableFuture` 병렬화 | 순차 46ms → 병렬 14ms (3.3배 단축) |
| **비동기 AI 분석** | `@Async` + `ThreadPoolTaskExecutor`로 AI 분석 논블로킹 처리 | 202 즉시 반환 |
| **Redis 캐싱** | 추천 API 결과 캐싱 + 취향 업데이트 시 `@CacheEvict` | 229ms → 15ms (15배 단축) |
| **QueryDSL** | 7가지 조건 동시 적용 가능한 동적 복합 검색 | if-else 분기 제거 |
| **AI 모델 교체** | Spring AI `ChatClient` 추상화로 Ollama ↔ OpenAI 설정만으로 교체 | 코드 변경 0줄 |

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Framework | Spring Boot 3.5, Spring Security, Spring AI |
| ORM | Spring Data JPA + Hibernate, QueryDSL 5.0 |
| DB | PostgreSQL (운영), H2 (테스트) |
| Cache | Redis + Spring Cache (`@Cacheable`, `@CacheEvict`) |
| 인증 | JWT (jjwt 0.12.6) |
| 비동기 | `@Async` + `ThreadPoolTaskExecutor` |
| 동시성 | JPA Optimistic Lock (`@Version`) |
| AI | Spring AI + OpenAI gpt-4o-mini (로컬: Ollama llama3 교체 가능) |
| 문서화 | Swagger / SpringDoc OpenAPI 3 |
| 빌드 | Gradle, Java 21 |

---

## 📊 ERD

```
┌──────────────────┐       ┌──────────────────────┐
│      USER        │       │    TASTING_NOTE       │
├──────────────────┤       ├──────────────────────┤
│ id (PK)          │──1:N──│ id (PK)              │
│ email (UNIQUE)   │       │ user_id (FK)         │
│ password         │       │ master_whisky_id (FK)│──N:0..1──┐
│ nickname         │       │ whisky_name          │          │
│ role             │       │ category             │          │
└──────────────────┘       │ sub_category         │          │
         │                 │ nose                 │          │
         │                 │ palate               │          │
         1:N               │ finish               │          │
         │                 │ rating               │          │
         ▼                 │ created_at           │          │
┌──────────────────┐       └──────────────────────┘          │
│  USER_PREFERENCE │                                          ▼
├──────────────────┤                             ┌──────────────────────┐
│ id (PK)          │                             │    MASTER_WHISKY     │
│ user_id (FK)     │                             ├──────────────────────┤
│ keyword          │                             │ id (PK)              │
│ score            │                             │ whisky_name          │
│ version          │← 낙관적 락                  │ distillery           │
└──────────────────┘                             │ category             │
                                                 │ sub_category         │
                                                 │ nose / palate /finish│
                                                 │ price / price_range  │
                                                 └──────────────────────┘
```

---

## 📡 API 명세

### 인증 (`/api/auth`) — 인증 불필요

| Method | URL | 설명 | 요청 Body |
|--------|-----|------|---------|
| POST | `/api/auth/signup` | 회원가입 | `email, password, nickname` |
| POST | `/api/auth/login` | 로그인 → JWT 발급 | `email, password` |

### 사용자 (`/api/users`) — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/users/me` | 내 정보 조회 |

### 테이스팅 노트 (`/api/notes`) — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/notes` | 노트 생성 |
| GET | `/api/notes` | 노트 목록 조회 (name/category/subCategory 검색 가능) |
| GET | `/api/notes/period` | 기간별 조회 (`start`, `end` 파라미터) |
| GET | `/api/notes/{id}` | 노트 단건 조회 |
| PUT | `/api/notes/{id}` | 노트 수정 |
| DELETE | `/api/notes/{id}` | 노트 삭제 |
| POST | `/api/notes/{id}/analyze` | AI 취향 분석 요청 (202 즉시 반환, 백그라운드 처리) |

### 위스키 검색 (`/api/master`) — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/master` | 전체 목록 조회 |
| GET | `/api/master/search` | 복합 조건 검색 (QueryDSL) |
| GET | `/api/master/recommend` | 키워드 기반 추천 |

**복합 검색 파라미터** (`/api/master/search`)

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| `name` | 이름 부분 일치 | `맥캘란` |
| `distillery` | 증류소 부분 일치 | `Macallan` |
| `category` | 대분류 완전 일치 | `스카치` |
| `subCategory` | 소분류 부분 일치 | `셰리` |
| `priceRange` | 가격대 완전 일치 | `10만원대` |
| `maxPrice` | 최대 가격 이하 | `100000` |
| `flavorKeyword` | 향미 통합 검색 (nose+palate+finish) | `피트` |

### 개인 추천 (`/api/recommend`) — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/recommend` | 취향 기반 개인화 추천 (Redis 캐싱) |
| GET | `/api/recommend?maxPrice=100000` | 가격 조건 포함 추천 |

---

## 🏗 아키텍처 흐름

```
클라이언트
   │
   ▼
JwtAuthenticationFilter  ← JWT 검증
   │
   ▼
Controller
   │
   ├── NoteService          ← 노트 CRUD (본인 데이터만)
   │
   ├── WhiskyAnalysisService ← @Async 비동기 AI 분석
   │       │
   │       ├── OpenAI API (gpt-4o-mini) ← 키워드 추출
   │       │
   │       └── PreferenceUpdateService  ← @Version 낙관적 락
   │               └── CompletableFuture 병렬 저장
   │
   ├── WhiskyRecommendService ← @Cacheable Redis 캐싱
   │
   └── MasterWhiskySearchService ← QueryDSL 동적 쿼리
```

---

## ⚙️ 로컬 실행 방법

### 사전 요구사항
- Java 21
- PostgreSQL
- Docker (Redis용)

### 1. Redis 실행
```bash
docker run -d --name whisky-redis -p 6379:6379 redis
```

### 2. PostgreSQL DB 생성
```sql
CREATE DATABASE whisky_db;
```

### 3. 환경 설정
`src/main/resources/application-secret.properties` 생성:
```properties
jwt.secret=${JWT_SECRET:your-jwt-secret-key-at-least-32-characters}
spring.ai.openai.api-key=${OPENAI_API_KEY:your-openai-api-key}
```

### 4. 실행
```bash
./gradlew bootRun
```

### 5. API 문서 확인
```
http://localhost:8080/swagger-ui.html
```

---

## 🔄 AI 모델 교체 방법

Spring AI `ChatClient` 추상화를 사용하여 **코드 변경 없이** 설정만으로 AI 모델을 교체할 수 있습니다.

**Ollama (로컬) 사용 시** — `build.gradle`
```groovy
implementation 'org.springframework.ai:spring-ai-ollama-spring-boot-starter'
// implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
```

**OpenAI (배포) 사용 시** — `build.gradle`
```groovy
// implementation 'org.springframework.ai:spring-ai-ollama-spring-boot-starter'
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
```

---

## 🧪 테스트

```bash
./gradlew test
```

| 테스트 클래스 | 내용 |
|------------|------|
| `PreferenceUpdateServiceTest` | 낙관적 락 동시성 검증 (CountDownLatch + ExecutorService) |
| `WhiskyAnalysisServiceTest` | AI 분석 후 키워드 DB 저장 검증 |
| `PreferenceUpdateBenchmarkTest` | 순차 vs 병렬 처리 성능 측정 |

---

## 📁 프로젝트 구조

```
src/main/java/com/whisky/note_app/
├── config/          # AsyncConfig, CacheConfig, QueryDslConfig, SecurityConfig
├── controller/      # Auth, Note, MasterWhisky, Recommend, User
├── dto/             # Request / Response DTO
├── entity/          # User, TastingNote, MasterWhisky, UserPreference
├── exception/       # GlobalExceptionHandler, 커스텀 예외
├── repository/      # JPA Repository + QueryDSL Custom
├── security/        # JwtAuthenticationFilter, JwtTokenProvider
├── service/         # 비즈니스 로직
└── util/            # JwtUtil
```

---

## 🚀 배포

- **서버**: Railway
- **DB**: Railway PostgreSQL
- **Cache**: Upstash Redis
- **배포 URL**: https://whiskynote-production.up.railway.app
- **API 문서 (Swagger)**: https://whiskynote-production.up.railway.app/swagger-ui.html

---

## 🧭 API 사용 가이드

> Swagger UI에서 직접 테스트하거나, 아래 순서대로 curl로 테스트할 수 있습니다.

### Step 1 — 회원가입

```bash
curl -X POST https://whiskynote-production.up.railway.app/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"your@email.com","password":"password123","nickname":"닉네임"}'
```

### Step 2 — 로그인 → JWT 토큰 발급

```bash
curl -X POST https://whiskynote-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your@email.com","password":"password123"}'
```

응답에서 `token` 값을 복사해두세요. 이후 모든 요청에 사용합니다.

### Step 3 — 테이스팅 노트 작성

```bash
curl -X POST https://whiskynote-production.up.railway.app/api/notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "whiskyName": "맥캘란 12년",
    "category": "스카치",
    "subCategory": "셰리",
    "nose": "달콤한 셰리, 바닐라, 건과일",
    "palate": "리치, 오렌지 필, 다크초콜릿",
    "finish": "긴 여운, 스파이시",
    "rating": 4.5
  }'
```

### Step 4 — AI 취향 분석 요청 (비동기, 202 즉시 반환)

```bash
curl -X POST https://whiskynote-production.up.railway.app/api/notes/{noteId}/analyze \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Step 5 — 취향 기반 개인화 추천 (Redis 캐싱)

```bash
curl https://whiskynote-production.up.railway.app/api/recommend \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 가격 조건 포함
curl "https://whiskynote-production.up.railway.app/api/recommend?maxPrice=100000" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Step 6 — 위스키 복합 검색 (QueryDSL 7조건)

```bash
# 피트향 스카치 위스키 중 10만원 이하 검색
curl "https://whiskynote-production.up.railway.app/api/master/search?category=스카치&flavorKeyword=피트&maxPrice=100000" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

> 💡 **Swagger에서 더 편리하게 테스트하려면:**
> 1. https://whiskynote-production.up.railway.app/swagger-ui.html 접속
> 2. `/api/auth/login` 으로 로그인 후 토큰 복사
> 3. 우측 상단 **Authorize** 버튼 클릭 → `Bearer {토큰}` 입력
> 4. 이후 모든 API 자유롭게 테스트 가능
