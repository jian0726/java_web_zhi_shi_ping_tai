CREATE DATABASE IF NOT EXISTS haoyou DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE haoyou;

-- 用户（FR-01~06）
CREATE TABLE sys_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone         VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    password_hash VARCHAR(100) COMMENT 'BCrypt 加密',
    nickname      VARCHAR(50),
    avatar        VARCHAR(255),
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色字典
CREATE TABLE sys_role (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(30) NOT NULL UNIQUE COMMENT 'READER/CREATOR/AUDITOR/ADMIN',
    role_name VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户-角色关联
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 创作者申请（FR-07~10）
CREATE TABLE creator_apply (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    reason       VARCHAR(500) COMMENT '申请理由',
    qualification VARCHAR(500) COMMENT '资质说明',
    status       TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
    audit_remark VARCHAR(500),
    audited_by   BIGINT,
    audited_at   DATETIME,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted      TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者申请';

-- 知识库文章（FR-11~17）
CREATE TABLE knowledge (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_no    VARCHAR(30) NOT NULL UNIQUE COMMENT 'KB-YYYYMMDD-XXXX',
    title         VARCHAR(200) NOT NULL,
    kb_type       VARCHAR(30) COMMENT '知识库类型',
    job_id        BIGINT COMMENT '岗位分类',
    summary       VARCHAR(500),
    display_type  VARCHAR(20) COMMENT '显示类型',
    cover_url     VARCHAR(255) COMMENT '封面',
    content       LONGTEXT COMMENT '富文本正文',
    status        TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审 2已发布 3驳回',
    author_id     BIGINT NOT NULL,
    current_version INT NOT NULL DEFAULT 1,
    published_at  DATETIME,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文章';

-- 附件（FR-13）
CREATE TABLE knowledge_attachment (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL,
    file_name   VARCHAR(200) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    file_size   BIGINT,
    file_type   VARCHAR(20) COMMENT 'PDF/JPG/PNG/DOCX',
    uploaded_by BIGINT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章附件';

-- 审核历史（FR-23）
CREATE TABLE knowledge_audit_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL,
    version     INT NOT NULL,
    action      TINYINT NOT NULL COMMENT '1通过 2驳回',
    remark      VARCHAR(500),
    auditor_id  BIGINT,
    audited_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录';

-- 岗位认知（FR-24~27）
CREATE TABLE job_cognition (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name      VARCHAR(100) NOT NULL,
    job_duty      VARCHAR(1000) COMMENT '岗位职责',
    job_require   VARCHAR(1000) COMMENT '任职要求',
    ability_model VARCHAR(1000) COMMENT '核心能力模型',
    work_scene    VARCHAR(1000) COMMENT '典型工作场景',
    status        TINYINT NOT NULL DEFAULT 1,
    created_by    BIGINT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位认知';

-- 站内通知（FR-32~34）
CREATE TABLE notify_message (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    title     VARCHAR(100) NOT NULL,
    content   VARCHAR(500),
    biz_type  VARCHAR(30) COMMENT 'SUBMIT/APPROVE/REJECT',
    biz_id    BIGINT,
    is_read   TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';
