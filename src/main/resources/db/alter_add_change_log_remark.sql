-- 已有库升级：变更记录增加备注字段
ALTER TABLE `le_equipment_change_log`
    ADD COLUMN `remark` VARCHAR(512) NULL COMMENT '变更备注' AFTER `source`;
