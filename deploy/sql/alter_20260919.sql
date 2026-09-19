-- 2026-09-19 读者端阶段一增量
-- 执行方式：mysql -uroot -p123456 < deploy/sql/alter_20260919.sql

USE haoyou;

-- 知识库文章增加订阅计数（分类页/详情页展示「xxxx人已订阅」）
ALTER TABLE knowledge
    ADD COLUMN subscribe_count INT NOT NULL DEFAULT 0 COMMENT '订阅数' AFTER published_at;

-- 订阅关系（个人中心「已订阅」列表）
CREATE TABLE IF NOT EXISTS subscription (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    knowledge_id BIGINT NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_knowledge (user_id, knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识订阅关系';

-- 角色字典（用户管理/创作者审核用；INSERT IGNORE 可重复执行）
INSERT IGNORE INTO sys_role (id, role_code, role_name) VALUES
(1, 'READER',  '读者'),
(2, 'CREATOR', '创作者'),
(3, 'AUDITOR', '审核专员'),
(4, 'ADMIN',   '平台管理员');

-- 知识库协作者（创作者端「管理协作者」）
CREATE TABLE IF NOT EXISTS knowledge_collaborator (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    role         VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER/MEMBER',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_knowledge_user (knowledge_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库协作者';
