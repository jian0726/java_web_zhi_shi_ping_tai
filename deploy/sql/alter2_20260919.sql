-- 2026-09-19 第二批增量：创作者认证字段（8个人信息 设计稿）
-- 注意：alter_20260919.sql 只需执行一次；本文件独立执行。
-- 执行方式：mysql -uroot -p123456 < deploy/sql/alter2_20260919.sql

USE haoyou;

ALTER TABLE sys_user
    ADD COLUMN occupation  VARCHAR(50)  COMMENT '职业' AFTER avatar,
    ADD COLUMN work_years  VARCHAR(20)  COMMENT '工作年限' AFTER occupation,
    ADD COLUMN work_status VARCHAR(20)  COMMENT '工作状态(在职/自由职业/学生/待业)' AFTER work_years,
    ADD COLUMN id_card     VARCHAR(30)  COMMENT '身份证号' AFTER work_status,
    ADD COLUMN work_proof  VARCHAR(255) COMMENT '工作证明文件URL' AFTER id_card;
