-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE shop;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 字典表
CREATE TABLE IF NOT EXISTS dictionary (
    word VARCHAR(160) NOT NULL PRIMARY KEY,
    translation VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
create table CET4
(
    word        varchar(256) not null primary key ,
    translation varchar(512) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
create table CET6
(
    word        varchar(256) not null primary key ,
    translation varchar(512) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table test_record
(
    id                  bigint auto_increment primary key,
    user_id             bigint       not null,
    question_type       varchar(20)  not null,
    project             varchar(20)  not null,
    word                varchar(256) not null,
    correct_translation varchar(512) not null,
    user_answer         varchar(512) null,
    is_correct          tinyint(1)   not null default 0,
    create_time         datetime     not null,
    index idx_user_id (user_id),
    index idx_project (project),
    index idx_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

