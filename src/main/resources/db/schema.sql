DROP TABLE IF EXISTS `id_sequence`;
CREATE TABLE `id_sequence`
(
    `entity_type`   VARCHAR(32) NOT NULL PRIMARY KEY,
    `current_value` BIGINT      NOT NULL DEFAULT 0,
    `step`          INT         NOT NULL DEFAULT 1,
    `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO `id_sequence` (`entity_type`, `current_value`, `step`)
VALUES ('USR', 0, 1),
       ('EQP', 0, 1),
       ('ROM', 0, 1),
       ('CHG', 0, 1);

DROP TABLE IF EXISTS `le_user`;
CREATE TABLE `le_user`
(
    `id`         VARCHAR(64)  NOT NULL PRIMARY KEY,
    `username`   VARCHAR(64)  NOT NULL,
    `name`       VARCHAR(64)  NULL,
    `password`   VARCHAR(512) NOT NULL,
    `status`     VARCHAR(32)  NOT NULL DEFAULT 'ENABLED',
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

DROP TABLE IF EXISTS `le_room`;
CREATE TABLE `le_room`
(
    `id`          VARCHAR(64)  NOT NULL PRIMARY KEY,
    `room_code`   VARCHAR(64)  NOT NULL COMMENT '房间编号，如 A465',
    `room_name`   VARCHAR(128) NULL COMMENT '房间显示名',
    `building`    VARCHAR(128) NULL COMMENT '楼宇',
    `status`      VARCHAR(32)  NOT NULL DEFAULT 'ENABLED',
    `created_at`  DATETIME     NOT NULL,
    `updated_at`  DATETIME     NOT NULL,
    UNIQUE KEY `uk_room_code` (`room_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='实验室房间';

DROP TABLE IF EXISTS `le_equipment_change_log`;
CREATE TABLE `le_equipment_change_log`
(
    `id`           VARCHAR(64)  NOT NULL PRIMARY KEY,
    `equipment_id` VARCHAR(64)  NOT NULL,
    `asset_code`   VARCHAR(64)  NOT NULL,
    `field_name`   VARCHAR(64)  NOT NULL,
    `old_value`    VARCHAR(512) NULL,
    `new_value`    VARCHAR(512) NULL,
    `operator_id`  VARCHAR(64)  NULL,
    `operator_name` VARCHAR(64) NULL,
    `source`       VARCHAR(32)  NOT NULL DEFAULT 'SCAN',
    `remark`       VARCHAR(512) NULL COMMENT '变更备注',
    `created_at`   DATETIME     NOT NULL,
    KEY `idx_equipment_id` (`equipment_id`),
    KEY `idx_asset_code` (`asset_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='设备变更日志';

DROP TABLE IF EXISTS `le_equipment`;
CREATE TABLE `le_equipment`
(
    `id`               VARCHAR(64)    NOT NULL PRIMARY KEY,
    `asset_code`       VARCHAR(64)    NOT NULL COMMENT '资产编号',
    `name`             VARCHAR(256)   NOT NULL,
    `brand`            VARCHAR(128)   NULL,
    `model`            VARCHAR(256)   NULL,
    `serial_no`        VARCHAR(256)   NULL COMMENT '出厂号',
    `spec`             TEXT           NULL,
    `quantity`         DECIMAL(12, 2) NULL,
    `unit`             VARCHAR(32)    NULL,
    `unit_price`       DECIMAL(14, 2) NULL,
    `book_value`       DECIMAL(14, 2) NULL,
    `card_status`      VARCHAR(32)    NULL COMMENT '卡片状态',
    `usage_status`     VARCHAR(32)    NULL COMMENT '现状',
    `room_id`          VARCHAR(64)    NULL,
    `location_raw`     VARCHAR(256)   NULL COMMENT '原始安置地点',
    `location_note`    VARCHAR(256)   NULL COMMENT '安置地点备注',
    `department`       VARCHAR(256)   NULL,
    `building`         VARCHAR(128)   NULL,
    `custodian`        VARCHAR(256)   NULL,
    `purchase_date`    DATE           NULL,
    `scrap_date`       DATE           NULL,
    `supplier`         VARCHAR(256)   NULL,
    `manufacturer`     VARCHAR(256)   NULL,
    `is_abnormal`      TINYINT(1)     NOT NULL DEFAULT 0,
    `ext_info`         MEDIUMTEXT     NULL,
    `created_at`       DATETIME       NOT NULL,
    `updated_at`       DATETIME       NOT NULL,
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_usage_status` (`usage_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='高值设备';
