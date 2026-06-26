-- =====================================================
-- DROP TABLES
-- =====================================================

DROP TABLE IF EXISTS tb_notifications;
DROP TABLE IF EXISTS tb_review_likes;
DROP TABLE IF EXISTS tb_comments;

DROP TABLE IF EXISTS tb_book_rankings;
DROP TABLE IF EXISTS tb_review_rankings;
DROP TABLE IF EXISTS tb_user_rankings;

DROP TABLE IF EXISTS tb_refresh_token;
DROP TABLE IF EXISTS tb_binary_content;

DROP TABLE IF EXISTS tb_reviews;
DROP TABLE IF EXISTS tb_books;
DROP TABLE IF EXISTS tb_users;


-- =====================================================
-- DROP ENUM TYPES
-- =====================================================

DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS auth_provider;
DROP TYPE IF EXISTS book_category;


-- =====================================================
-- ENUM 타입
-- =====================================================

CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'USER'
    );

CREATE TYPE auth_provider AS ENUM (
    'LOCAL',
    'GOOGLE',
    'NAVER'
    );


CREATE TYPE book_category AS ENUM (
    'NOVEL',
    'POETRY_ESSAY',
    'HUMANITIES',
    'FAMILY_PARENTING',
    'COOKING',
    'HEALTH',
    'HOBBY_SPORTS',
    'ECONOMY_BUSINESS',
    'SELF_DEVELOPMENT',
    'POLITICS_SOCIETY',
    'HISTORY_CULTURE',
    'RELIGION',
    'ART_POP_CULTURE',
    'MIDDLE_HIGH_SCHOOL',
    'TECHNOLOGY_ENGINEERING',
    'FOREIGN_LANGUAGE',
    'SCIENCE',
    'EXAM_JOB',
    'TRAVEL',
    'COMPUTER_IT',
    'MAGAZINE',
    'TEEN',
    'ELEMENTARY_STUDY',
    'INFANT',
    'CHILDREN',
    'COMICS',
    'UNIVERSITY_TEXTBOOK',
    'KOREA_INTRODUCTION'
    );


-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE tb_users (
    id              UUID NOT NULL,
    username        VARCHAR(50) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    password        VARCHAR(255) NULL,
    role            user_role DEFAULT 'USER' NOT NULL,
    provider        auth_provider DEFAULT 'LOCAL' NULL,
    provider_id     VARCHAR(255) NULL,
    last_login_at   TIMESTAMPTZ NULL,
    is_deleted      BOOLEAN DEFAULT false NOT NULL,
    deleted_at      TIMESTAMPTZ NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_users PRIMARY KEY (id),
    CONSTRAINT uk_tb_users_username UNIQUE (username),
    CONSTRAINT uk_tb_users_email UNIQUE (email),
    CONSTRAINT uk_tb_users_provider_provider_id UNIQUE (provider, provider_id)
);

-- =====================================================
-- BOOKS
-- =====================================================
CREATE TABLE tb_books (
    id              UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(100) NOT NULL,
    description     TEXT NOT NULL,
    publisher       VARCHAR(100) NOT NULL,
    published_date  DATE NOT NULL,
    isbn            VARCHAR(20) NOT NULL,
    category        book_category NOT NULL,
    review_cnt      INTEGER DEFAULT 0 NOT NULL,
    rating          DOUBLE PRECISION DEFAULT 0 NOT NULL,
    is_deleted      BOOLEAN DEFAULT false NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_books PRIMARY KEY (id),
    CONSTRAINT uk_tb_books_isbn UNIQUE (isbn),
    CONSTRAINT chk_tb_books_review_cnt CHECK (review_cnt >= 0),
    CONSTRAINT chk_tb_books_rating CHECK (rating >= 0 AND rating <= 5)
);

-- =====================================================
-- BINARY CONTENT
-- 도서 썸네일 1:1 연결
-- =====================================================
CREATE TABLE tb_binary_content (
    id                  UUID NOT NULL,
    book_id             UUID NOT NULL,
    origin_file_url     VARCHAR(255) NOT NULL,
    renamed_file_url    VARCHAR(255) NOT NULL,
    size                BIGINT NOT NULL,
    content_type        VARCHAR(100) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_binary_content PRIMARY KEY (id),
    CONSTRAINT uk_tb_binary_content_book_id UNIQUE (book_id),

    CONSTRAINT fk_tb_binary_content_book
       FOREIGN KEY (book_id)
           REFERENCES tb_books (id)
           ON DELETE CASCADE,

    CONSTRAINT chk_tb_binary_content_size CHECK (size > 0)
);

-- =====================================================
-- REVIEWS
-- =====================================================
CREATE TABLE tb_reviews (
    id              UUID NOT NULL,
    book_id         UUID NOT NULL,
    user_id         UUID NOT NULL,
    content         TEXT NOT NULL,
    rating          INTEGER NOT NULL,
    like_cnt        INTEGER DEFAULT 0 NOT NULL,
    comment_cnt     INTEGER DEFAULT 0 NOT NULL,
    is_deleted      BOOLEAN DEFAULT false NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_reviews PRIMARY KEY (id),

    CONSTRAINT fk_tb_reviews_book
        FOREIGN KEY (book_id)
            REFERENCES tb_books (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_tb_reviews_user
        FOREIGN KEY (user_id)
            REFERENCES tb_users (id)
            ON DELETE CASCADE,

    -- 도서별 사용자 1명당 리뷰 1개
    CONSTRAINT uk_tb_reviews_book_user UNIQUE (book_id, user_id),

    CONSTRAINT chk_tb_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_tb_reviews_like_cnt CHECK (like_cnt >= 0),
    CONSTRAINT chk_tb_reviews_comment_cnt CHECK (comment_cnt >= 0),
    CONSTRAINT chk_tb_reviews_content_not_blank CHECK (length(trim(content)) > 0)
);

-- =====================================================
-- COMMENTS
-- =====================================================
CREATE TABLE tb_comments (
    id              UUID NOT NULL,
    review_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    content         TEXT NOT NULL,
    is_deleted      BOOLEAN DEFAULT false NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_comments PRIMARY KEY (id),

    CONSTRAINT fk_tb_comments_review
     FOREIGN KEY (review_id)
         REFERENCES tb_reviews (id)
         ON DELETE CASCADE,

    CONSTRAINT fk_tb_comments_user
     FOREIGN KEY (user_id)
         REFERENCES tb_users (id)
         ON DELETE CASCADE,

    CONSTRAINT chk_tb_comments_content_not_blank CHECK (length(trim(content)) > 0)
);

-- =====================================================
-- REVIEW LIKES
-- liked 컬럼 제거: 레코드 존재 여부로 좋아요 상태 판단 (insert/delete 방식)
-- =====================================================
CREATE TABLE tb_review_likes (
    id              UUID NOT NULL,
    review_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_review_likes PRIMARY KEY (id),

    CONSTRAINT fk_tb_review_likes_review
     FOREIGN KEY (review_id)
         REFERENCES tb_reviews (id)
         ON DELETE CASCADE,

    CONSTRAINT fk_tb_review_likes_user
     FOREIGN KEY (user_id)
         REFERENCES tb_users (id)
         ON DELETE CASCADE,

    -- 사용자 1명이 리뷰 1개에 좋아요 기록 1개만
    CONSTRAINT uk_tb_review_likes_review_user UNIQUE (review_id, user_id)
);

-- =====================================================
-- NOTIFICATIONS
-- =====================================================
CREATE TABLE tb_notifications (
    id              UUID NOT NULL,
    user_id         UUID NOT NULL,
    review_id       UUID NOT NULL,
    message         TEXT NOT NULL,
    noti_type       VARCHAR(30) NOT NULL,
    confirmed       BOOLEAN DEFAULT false NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_notifications PRIMARY KEY (id),

    CONSTRAINT fk_tb_notifications_user
      FOREIGN KEY (user_id)
          REFERENCES tb_users (id)
          ON DELETE CASCADE,

    CONSTRAINT fk_tb_notifications_review
      FOREIGN KEY (review_id)
          REFERENCES tb_reviews (id)
          ON DELETE CASCADE,

    CONSTRAINT chk_tb_notifications_type
      CHECK (noti_type IN ('REVIEW_LIKE', 'REVIEW_COMMENT', 'REVIEW_RANKED')),

    CONSTRAINT chk_tb_notifications_message_not_blank CHECK (length(trim(message)) > 0)
);

-- =====================================================
-- REFRESH TOKEN
-- =====================================================
CREATE TABLE tb_refresh_token (
    id              UUID NOT NULL,
    user_id         UUID NOT NULL,
    token           VARCHAR(512) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    is_revoked      BOOLEAN DEFAULT false NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_refresh_token PRIMARY KEY (id),

    CONSTRAINT fk_tb_refresh_token_user
      FOREIGN KEY (user_id)
          REFERENCES tb_users (id)
          ON DELETE CASCADE,

    CONSTRAINT uk_tb_refresh_token_token UNIQUE (token)
);

-- =====================================================
-- BOOK RANKINGS
-- =====================================================
CREATE TABLE tb_book_rankings (
    id              UUID NOT NULL,
    book_id         UUID NOT NULL,
    period_type     VARCHAR(20) NOT NULL,
    rank_position   INTEGER NOT NULL,
    score           DECIMAL(10, 2) NOT NULL,
    calculated_at   TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_book_rankings PRIMARY KEY (id),

    CONSTRAINT fk_tb_book_rankings_book
      FOREIGN KEY (book_id)
          REFERENCES tb_books (id)
          ON DELETE CASCADE,

    CONSTRAINT chk_tb_book_rankings_period_type
      CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),

    CONSTRAINT chk_tb_book_rankings_rank_position CHECK (rank_position > 0),
    CONSTRAINT chk_tb_book_rankings_score CHECK (score >= 0),

    -- 같은 집계 시점에 같은 도서가 같은 기간 랭킹에 중복 등록되지 않게
    CONSTRAINT uk_tb_book_rankings_book_period_calculated
      UNIQUE (book_id, period_type, calculated_at),

    -- 같은 집계 시점에 같은 순위가 중복되지 않게
    CONSTRAINT uk_tb_book_rankings_period_rank_calculated
      UNIQUE (period_type, rank_position, calculated_at)
);

-- =====================================================
-- REVIEW RANKINGS
-- =====================================================
CREATE TABLE tb_review_rankings (
    id              UUID NOT NULL,
    review_id       UUID NOT NULL,
    period_type     VARCHAR(20) NOT NULL,
    rank_position   INTEGER NOT NULL,
    score           DECIMAL(10, 2) NOT NULL,
    calculated_at   TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_review_rankings PRIMARY KEY (id),

    CONSTRAINT fk_tb_review_rankings_review
        FOREIGN KEY (review_id)
            REFERENCES tb_reviews (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_tb_review_rankings_period_type
        CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),

    CONSTRAINT chk_tb_review_rankings_rank_position CHECK (rank_position > 0),
    CONSTRAINT chk_tb_review_rankings_score CHECK (score >= 0),

    CONSTRAINT uk_tb_review_rankings_review_period_calculated
        UNIQUE (review_id, period_type, calculated_at),

    CONSTRAINT uk_tb_review_rankings_period_rank_calculated
        UNIQUE (period_type, rank_position, calculated_at)
);

-- =====================================================
-- USER RANKINGS
-- =====================================================
CREATE TABLE tb_user_rankings (
    id              UUID NOT NULL,
    user_id         UUID NOT NULL,
    period_type     VARCHAR(20) NOT NULL,
    rank_position   INTEGER NOT NULL,
    score           DECIMAL(10, 2) NOT NULL,
    calculated_at   TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tb_user_rankings PRIMARY KEY (id),

    CONSTRAINT fk_tb_user_rankings_user
      FOREIGN KEY (user_id)
          REFERENCES tb_users (id)
          ON DELETE CASCADE,

    CONSTRAINT chk_tb_user_rankings_period_type
      CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),

    CONSTRAINT chk_tb_user_rankings_rank_position CHECK (rank_position > 0),
    CONSTRAINT chk_tb_user_rankings_score CHECK (score >= 0),

    CONSTRAINT uk_tb_user_rankings_user_period_calculated
      UNIQUE (user_id, period_type, calculated_at),

    CONSTRAINT uk_tb_user_rankings_period_rank_calculated
      UNIQUE (period_type, rank_position, calculated_at)
);


-- =====================================================
-- INDEXES
-- =====================================================

-- tb_books
CREATE INDEX idx_tb_books_title         ON tb_books (title);
CREATE INDEX idx_tb_books_author        ON tb_books (author);
CREATE INDEX idx_tb_books_isbn          ON tb_books (isbn);
CREATE INDEX idx_tb_books_published_date ON tb_books (published_date DESC);
CREATE INDEX idx_tb_books_rating        ON tb_books (rating DESC);
CREATE INDEX idx_tb_books_review_cnt    ON tb_books (review_cnt DESC);
CREATE INDEX idx_tb_books_is_deleted    ON tb_books (is_deleted);

-- tb_reviews
CREATE INDEX idx_tb_reviews_book_id     ON tb_reviews (book_id);
CREATE INDEX idx_tb_reviews_user_id     ON tb_reviews (user_id);
CREATE INDEX idx_tb_reviews_created_at  ON tb_reviews (created_at DESC);
CREATE INDEX idx_tb_reviews_rating      ON tb_reviews (rating DESC);
CREATE INDEX idx_tb_reviews_is_deleted  ON tb_reviews (is_deleted);

-- tb_comments
CREATE INDEX idx_tb_comments_review_id  ON tb_comments (review_id);
CREATE INDEX idx_tb_comments_created_at ON tb_comments (created_at DESC);
CREATE INDEX idx_tb_comments_is_deleted ON tb_comments (is_deleted);

-- tb_review_likes
CREATE INDEX idx_tb_review_likes_review_id ON tb_review_likes (review_id);

-- tb_notifications
CREATE INDEX idx_tb_notifications_user_id    ON tb_notifications (user_id);
CREATE INDEX idx_tb_notifications_created_at ON tb_notifications (created_at DESC);
CREATE INDEX idx_tb_notifications_confirmed  ON tb_notifications (confirmed);

-- tb_refresh_token
CREATE INDEX idx_tb_refresh_token_user_id    ON tb_refresh_token (user_id);
CREATE INDEX idx_tb_refresh_token_expires_at ON tb_refresh_token (expires_at);
CREATE INDEX idx_tb_refresh_token_is_revoked ON tb_refresh_token (is_revoked);

-- tb_book_rankings: 기간별 최신 집계 순위 조회
CREATE INDEX idx_tb_book_rankings_period_calculated
    ON tb_book_rankings (period_type, calculated_at DESC, rank_position);

-- tb_review_rankings: 기간별 최신 집계 순위 조회
CREATE INDEX idx_tb_review_rankings_period_calculated
    ON tb_review_rankings (period_type, calculated_at DESC, rank_position);

-- tb_user_rankings: 기간별 최신 집계 순위 조회
CREATE INDEX idx_tb_user_rankings_period_calculated
    ON tb_user_rankings (period_type, calculated_at DESC, rank_position);
