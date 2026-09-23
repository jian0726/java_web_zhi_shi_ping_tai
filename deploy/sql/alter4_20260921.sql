-- ============================================================
-- 2026-09-21 第四批增量：用户服务数据库补强
-- 对应文档：《好YOU经验知识共享平台 — 用户服务数据库设计说明书》
-- 内容：
--   1. sys_role / sys_user_role 补强（主键、时间戳、外键索引）
--   2. login_log     登录审计日志（NFR-14 越权审计）
--   3. user_device   登录设备与 Refresh Token（FR-03 扫码登录 / FR-04 自动登录）
--   4. user_consent  用户授权同意留痕（NFR-13《个人信息保护法》合规）
--   5. 现有表索引补强
--   6. sys_user 敏感字段密文扩长（修复 D-02 明文存储）
-- 执行方式：mysql -uroot -p123456 < deploy/sql/alter4_20260921.sql
-- 幂等说明：全部语句可重复执行（IF NOT EXISTS / 忽略已存在错误）
-- ============================================================

USE haoyou;

-- ------------------------------------------------------------
-- 1. sys_user_role 补强：新增自增主键与创建时间
--    原表主键为 (user_id, role_id) 联合主键，MyBatis-Plus
--    单表操作无独立 id，扩展性受限于「无法记录授权时间/授权人」。
-- ------------------------------------------------------------
ALTER TABLE sys_user_role
    ADD COLUMN id         BIGINT   NOT NULL AUTO_INCREMENT FIRST,
    ADD COLUMN granted_by BIGINT   NULL COMMENT '授权操作人ID（ADMIN）' AFTER role_id,
    ADD COLUMN granted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间' AFTER granted_by,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id),
    ADD UNIQUE KEY uk_user_role (user_id, role_id),
    ADD KEY idx_role_id (role_id);

-- ------------------------------------------------------------
-- 2. login_log 登录审计日志
--    需求依据：NFR-14「不同角色须隔离数据访问权限，禁止越权操作」；
--             NFR-09「核心时段异常 15 分钟内通知运维」需可追溯登录异常。
--    写入策略：异步写（@Async），失败不阻断登录主流程。
--    保留策略：热数据 90 天，冷数据归档至 login_log_archive。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NULL COMMENT '用户ID；登录失败且账号不存在时为 NULL',
    phone       VARCHAR(20)  NULL COMMENT '登录手机号（脱敏前原始值，仅供风控排查）',
    login_type  VARCHAR(20)  NOT NULL DEFAULT 'PASSWORD' COMMENT 'PASSWORD密码 / SMS短信 / WECHAT微信扫码 / AUTO自动登录',
    login_result TINYINT     NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    fail_reason VARCHAR(100) NULL COMMENT '失败原因：密码错误/账号禁用/验证码失效等',
    ip          VARCHAR(45)  NULL COMMENT '客户端IP（兼容IPv6，最长45字符）',
    user_agent  VARCHAR(500) NULL COMMENT '浏览器UA',
    device_id   VARCHAR(64)  NULL COMMENT '设备指纹，关联 user_device.device_id',
    login_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    KEY idx_user_time (user_id, login_at),
    KEY idx_phone_time (phone, login_at),
    KEY idx_login_at (login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录审计日志';

-- ------------------------------------------------------------
-- 3. user_device 登录设备与 Refresh Token
--    需求依据：FR-03「微信扫码登录（扫码后手机端确认）」
--             FR-04「用户在有效期内自动登录」
--             FR-06「主动退出登录」——退出需可精确吊销单个设备的令牌。
--    设计说明：
--      access_token  短期（24h，与 haoyou.jwt.expire-hours 一致），无状态不落库；
--      refresh_token 长期（默认 7 天），仅存 SHA-256 摘要，防库泄露后被直接复用。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_device (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    device_id     VARCHAR(64)  NOT NULL COMMENT '设备指纹（前端生成UUID并持久化于localStorage）',
    device_name   VARCHAR(100) NULL COMMENT '设备名称，如「Chrome on Windows」',
    os            VARCHAR(50)  NULL COMMENT '操作系统',
    browser       VARCHAR(50)  NULL COMMENT '浏览器',
    refresh_token VARCHAR(128) NOT NULL COMMENT 'Refresh Token 的 SHA-256 摘要（HEX）',
    expire_at     DATETIME     NOT NULL COMMENT 'Refresh Token 过期时间',
    last_active_at DATETIME    NULL COMMENT '最近活跃时间，自动续期时更新',
    revoked       TINYINT      NOT NULL DEFAULT 0 COMMENT '0有效 1已吊销（退出登录/改密后全量吊销）',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_device (user_id, device_id),
    KEY idx_refresh_token (refresh_token),
    KEY idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录设备与Refresh Token';

-- ------------------------------------------------------------
-- 4. user_consent 用户授权同意留痕
--    需求依据：NFR-13「系统须满足《个人信息保护法》相关要求，
--             支持用户注销账号后删除个人信息」。
--    合规要点：须能举证「用户在何时同意了哪个版本的哪份协议」，
--             故记录 agreement_version 与 IP，且本表不参与物理删除，
--             仅在注销时做匿名化（user_id 置 0，保留计数性证据）。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_consent (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT      NOT NULL COMMENT '用户ID',
    agreement_type    VARCHAR(30) NOT NULL COMMENT '协议类型：USER_AGREEMENT用户协议 / PRIVACY隐私政策 / MARKETING营销推送',
    agreement_version VARCHAR(20) NOT NULL COMMENT '协议版本号，如 v1.0',
    agreed            TINYINT     NOT NULL DEFAULT 1 COMMENT '1同意 0拒绝（营销类可撤回）',
    ip                VARCHAR(45) NULL COMMENT '同意时的客户端IP（举证用）',
    agreed_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同意时间',
    UNIQUE KEY uk_user_type_version (user_id, agreement_type, agreement_version),
    KEY idx_agreement_type (agreement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户授权同意记录';

-- ------------------------------------------------------------
-- 5. 现有表索引补强
-- ------------------------------------------------------------
-- sys_user：管理端按状态筛选 + 按手机号/昵称关键字检索（UserAdminController.users）
-- phone 已由 UNIQUE 约束隐含索引，无需重复创建；
-- nickname 前缀匹配无法命中索引，此处不建（数据量级 < 10 万，全表扫描可接受）
ALTER TABLE sys_user
    ADD KEY idx_status_created (status, created_at);

-- creator_apply：创作者审核列表按状态倒序分页
ALTER TABLE creator_apply
    ADD KEY idx_status_created (status, created_at),
    ADD KEY idx_user_id (user_id);

-- ------------------------------------------------------------
-- 6. 敏感字段密文扩长（配合 AES-256-GCM 加密存储，修复 D-02）
--    密文格式 = Base64(IV(12) ‖ CipherText ‖ Tag(16))，长度约为明文两倍。
--    id_card 原 VARCHAR(30) 仅够放明文身份证号（18 位），
--    加密后 Base64 串长度约 60~90 字符，故扩至 VARCHAR(255) 留足余量。
--    work_proof 由「明文文本」改为「文件存储路径」，同步扩长。
--    注意：本语句只改列定义，不做数据迁移；
--         存量明文数据需在应用侧用同一 AesGcmCryptoUtil 刷写，见部署手册。
-- ------------------------------------------------------------
ALTER TABLE sys_user
    MODIFY COLUMN id_card    VARCHAR(255) NULL COMMENT '身份证号（AES-256-GCM 密文，Base64 编码）',
    MODIFY COLUMN work_proof VARCHAR(512) NULL COMMENT '在职证明（文件存储路径）';
