-- 设备借用功能：用户/设备增量 + 借用申请表（已有库执行，仅执行一次）
-- 若某步报 Duplicate column / Duplicate key，说明已执行过，可改跑剩余分步脚本：
--   alter_add_user_student.sql / alter_add_equipment_borrow.sql / alter_create_borrow_table.sql

INSERT INTO `id_sequence` (`entity_type`, `current_value`, `step`)
VALUES ('BRW', 0, 1)
ON DUPLICATE KEY UPDATE `entity_type` = `entity_type`;

ALTER TABLE `le_user`
    ADD COLUMN `user_type` VARCHAR(32) NOT NULL DEFAULT 'ASSISTANT'
        COMMENT 'ADMIN/ASSISTANT/STUDENT' AFTER `status`,
    ADD COLUMN `student_no` VARCHAR(64) NULL COMMENT '学号，仅 STUDENT' AFTER `user_type`,
    ADD COLUMN `phone` VARCHAR(32) NULL COMMENT '手机号' AFTER `student_no`;

ALTER TABLE `le_user`
    ADD UNIQUE KEY `uk_student_no` (`student_no`);

UPDATE `le_user` SET `user_type` = 'ADMIN' WHERE `username` = 'admin';
UPDATE `le_user` SET `user_type` = 'ASSISTANT' WHERE `username` <> 'admin';

ALTER TABLE `le_equipment`
    ADD COLUMN `borrowable` TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '是否开放借用 0否 1是' AFTER `is_abnormal`,
    ADD COLUMN `allowed_borrow_types` VARCHAR(32) NOT NULL DEFAULT 'ALL'
        COMMENT 'ALL/EXTERNAL/IN_LAB' AFTER `borrowable`;

ALTER TABLE `le_equipment`
    ADD INDEX `idx_borrowable` (`borrowable`);

CREATE TABLE IF NOT EXISTS `le_equipment_borrow` (
    `id`                    VARCHAR(64)  NOT NULL PRIMARY KEY,
    `equipment_id`          VARCHAR(64)  NOT NULL,
    `asset_code`            VARCHAR(64)  NOT NULL,
    `equipment_name`        VARCHAR(256) NOT NULL,
    `borrow_type`           VARCHAR(32)  NOT NULL COMMENT 'EXTERNAL/IN_LAB',
    `status`                VARCHAR(32)  NOT NULL COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED/RETURNED',
    `expected_start_at`     DATETIME     NOT NULL,
    `expected_end_at`       DATETIME     NOT NULL,
    `actual_start_at`       DATETIME     NULL,
    `actual_end_at`         DATETIME     NULL,
    `borrower_name`         VARCHAR(64)  NOT NULL,
    `student_no`            VARCHAR(64)  NOT NULL,
    `borrower_phone`        VARCHAR(32) NOT NULL,
    `purpose`               VARCHAR(256) NULL,
    `destination`           VARCHAR(256) NULL,
    `remark`                VARCHAR(512) NULL,
    `applicant_user_id`     VARCHAR(64)  NULL,
    `approver_id`           VARCHAR(64)  NULL,
    `approver_name`         VARCHAR(64)  NULL,
    `approve_remark`        VARCHAR(512) NULL,
    `approved_at`           DATETIME     NULL,
    `returner_id`           VARCHAR(64)  NULL,
    `returner_name`         VARCHAR(64)  NULL,
    `return_remark`         VARCHAR(512) NULL,
    `prev_usage_status`     VARCHAR(32)  NULL,
    `created_at`            DATETIME     NOT NULL,
    `updated_at`            DATETIME     NOT NULL,
    KEY `idx_equipment_status` (`equipment_id`, `status`),
    KEY `idx_equipment_type_status` (`equipment_id`, `borrow_type`, `status`),
    KEY `idx_student_no` (`student_no`),
    KEY `idx_status_created` (`status`, `created_at`),
    KEY `idx_asset_code` (`asset_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '设备借用申请';
