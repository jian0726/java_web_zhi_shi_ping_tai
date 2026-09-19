-- 2026-09-19 第三批增量：知识库 → 知识模块 → 文章 三级结构（FR-11~17 结构重构）
-- 独立执行：mysql -uroot -p123456 < deploy/sql/alter3_20260919.sql
-- 注意：subscription 表会重建（本地开发数据不保留）；旧 knowledge 演示数据不迁移。

USE haoyou;

-- 知识库（创作者创建与管理的主体，审核对象）
CREATE TABLE IF NOT EXISTS knowledge_base (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    kb_no          VARCHAR(30) NOT NULL UNIQUE COMMENT 'KB-YYYYMMDD-XXXX',
    name           VARCHAR(200) NOT NULL COMMENT '知识库名称',
    kb_type        VARCHAR(30) COMMENT '知识类别',
    summary        VARCHAR(500) COMMENT '知识库摘要',
    intro          LONGTEXT COMMENT '课程介绍（富文本）',
    display_type   VARCHAR(20) COMMENT '显示类型(公开/私有/隐藏)',
    cover_url      VARCHAR(255) COMMENT '封面',
    status         TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审 2已发布 3驳回',
    author_id      BIGINT NOT NULL,
    current_version INT NOT NULL DEFAULT 1,
    subscribe_count INT NOT NULL DEFAULT 0,
    published_at   DATETIME,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库';

-- 知识模块（知识库下的章节）
CREATE TABLE IF NOT EXISTS knowledge_module (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    base_id     BIGINT NOT NULL,
    module_name VARCHAR(200) NOT NULL COMMENT '模块/章节名',
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识模块';

-- 文章挂到模块
ALTER TABLE knowledge
    ADD COLUMN base_id   BIGINT COMMENT '所属知识库' AFTER kb_type,
    ADD COLUMN module_id BIGINT COMMENT '所属知识模块' AFTER base_id;

-- 订阅改为订阅知识库（重建）
DROP TABLE IF EXISTS subscription;
CREATE TABLE subscription (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    base_id    BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_base (user_id, base_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库订阅关系';

-- 协作者表的 knowledge_id 列语义变更为 base_id（列名不动，避免破坏性改表）
