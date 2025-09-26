# Community Service

> MSA 구조 기반의 커뮤니티 기능 전담 마이크로서비스

## 📋 프로젝트 개요

Community Service는 MSA(마이크로서비스) 아키텍처에서 **커뮤니티 기능을 담당하는 독립적인 서비스**입니다.
게시글, 댓글, 좋아요, 첨부파일 관리 등 커뮤니티의 핵심 CRUD 기능을 제공합니다.

## 🏗️ 아키텍처

```
Frontend → API Gateway (Port 8080) → Community Service
                    ↓                        ↓
              /auth/** → Auth Service    게시글/댓글 CRUD
                                             ↓
                                      MySQL Database
```

### MSA 구조에서의 역할 분담
- **API Gateway**: 요청 라우팅 (`/auth/**` → Auth Service)
- **Auth Service**: 사용자 인증, JWT 토큰 관리
- **Community Service**: 게시글/댓글/좋아요 관리 ← **현재 서비스**

## 🛠️ 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Spring Data JPA**: 데이터 액세스
- **Spring Security**: 최소 설정 (인증은 Auth Service 위임)
- **MySQL**: 데이터베이스
- **Swagger/OpenAPI**: API 문서화
- **Lombok**: 코드 간소화

## 🚀 주요 기능

### 📝 게시글 관리
- 게시글 CRUD (생성, 조회, 수정, 삭제)
- 카테고리별 분류 (자유게시판, 가격정보, 술 리뷰, Q&A, 이벤트)
- 페이징 및 정렬 (최신순, 조회수순, 좋아요순)
- 태그 기반 검색 및 키워드 검색
- 조회수 자동 증가

### 💬 댓글 관리
- 댓글 CRUD
- 게시글별 댓글 목록 조회
- 익명/회원 댓글 구분 관리

### ❤️ 좋아요 시스템
- 게시글 좋아요/좋아요 취소
- 사용자별 좋아요한 게시글 목록 조회
- 중복 좋아요 방지

### 📎 첨부파일 관리
- 파일 업로드
- 첨부파일 목록 조회 및 삭제
- 파일 크기 및 타입 검증

### 👤 사용자 관리
- **회원 사용자**: Auth Service 연동 인증
- **익명 사용자**: 이메일/비밀번호 기반 인증
- 통합된 Authors 엔티티로 관리

## 📂 프로젝트 구조

```
src/main/java/com/example/communityservice/
├── controller/          # REST API 엔드포인트
│   ├── PostsController.java
│   └── CommentsController.java
├── service/             # 비즈니스 로직
│   ├── PostsService.java
│   ├── CommentsService.java
│   ├── AuthService.java
│   └── FileUploadService.java
├── repository/          # 데이터 액세스
│   ├── PostsRepository.java
│   ├── CommentsRepository.java
│   ├── AuthorsRepository.java
│   ├── PostLikesRepository.java
│   └── PostAttachmentsRepository.java
├── entity/              # JPA 엔티티
│   ├── Posts.java
│   ├── Comments.java
│   ├── Authors.java
│   ├── PostLikes.java
│   ├── PostAttachments.java
│   └── PostCategory.java
├── dto/                 # 데이터 전송 객체
│   ├── posts/
│   ├── comments/
│   └── auth/
└── global/              # 공통 기능
    ├── config/
    ├── exception/
    ├── common/
    └── util/
```

## 🔗 주요 API 엔드포인트 (총 19개)

### 게시글 관리
```http
GET    /api/posts                    # 게시글 목록 조회
GET    /api/posts/{id}               # 게시글 상세 조회
POST   /api/posts                    # 게시글 작성
PUT    /api/posts/{id}               # 게시글 수정
DELETE /api/posts/{id}               # 게시글 삭제
POST   /api/posts/{id}/verify        # 익명 게시글 인증
```

### 좋아요 관리
```http
POST   /api/posts/{id}/likes         # 좋아요 추가
DELETE /api/posts/{id}/likes         # 좋아요 취소
```

### 댓글 관리
```http
POST   /api/comments                 # 댓글 작성
PUT    /api/comments/{id}            # 댓글 수정
DELETE /api/comments/{id}            # 댓글 삭제
POST   /api/comments/{id}/verify     # 익명 댓글 인증
GET    /api/comments/author/{authorId} # 작성자별 댓글 조회
```

### 첨부파일 관리
```http
GET    /api/posts/{id}/attachments   # 첨부파일 목록
POST   /api/posts/{id}/attachments   # 파일 업로드
DELETE /api/posts/attachments/{id}   # 첨부파일 삭제
```

### 태그 및 사용자 관리
```http
GET    /api/posts/tags               # 모든 태그 목록 조회
GET    /api/posts/users/{userId}/liked # 사용자 좋아요 게시글 목록
GET    /api/posts/users/{userId}/posts # 사용자 작성 게시글 목록
```

## ⚙️ 환경 설정

### application.properties
```properties
# 서버 설정
server.port=9999

# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/community_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Auth Service 연동 (API Gateway 통해서)
auth.service.url=http://localhost:8080/auth

# 파일 업로드 설정
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

## 🗄️ 데이터베이스 스키마

### 주요 테이블
- **authors**: 통합 작성자 정보 (회원/익명)
- **posts**: 게시글
- **comments**: 댓글
- **post_likes**: 좋아요
- **post_attachments**: 첨부파일

## 🔍 주요 특징

### 🎯 MSA 설계 원칙
- **단일 책임**: 커뮤니티 기능만 담당
- **서비스 독립성**: Auth Service와 HTTP 통신으로 분리
- **데이터 일관성**: 각 서비스별 독립적인 데이터베이스

### 🛡️ 보안 설계
- JWT 토큰 기반 인증 (Auth Service 위임)
- 익명 사용자 이메일/비밀번호 암호화
- Spring Security 최소 설정

### ⚡ 성능 최적화
- N+1 문제 해결 (IN절과 JOIN 최적화)
- 페이징 처리로 메모리 효율성
- 인덱스 최적화

### 🧹 코드 품질
- 중복 로직 공통 메서드화
- 계층형 예외 처리
- 포괄적인 JavaDoc 문서화

---
**Community Service** - MSA 환경에서의 커뮤니티 기능 전담 서비스