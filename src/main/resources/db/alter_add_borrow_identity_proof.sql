-- 借用申请：学生身份证明（OSS URL）
ALTER TABLE `le_equipment_borrow`
    ADD COLUMN `identity_proof_url` VARCHAR(512) NULL
        COMMENT '学生身份证明图片 OSS URL' AFTER `borrower_phone`;
