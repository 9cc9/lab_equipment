package org.uestc.weglas.core.config;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.oss")
public class OssProperties {

    private boolean enabled = true;

    /** 例：oss-cn-beijing.aliyuncs.com（华北2 北京） */
    private String endpoint = "oss-cn-beijing.aliyuncs.com";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    /** 与线上 bucketName 一致 */
    private String bucketName = "order-merchant-image";

    /** 自定义 CDN/绑定域名，为空则用 bucket 默认域名 */
    private String domain = "";

    /** 与线上 object-prefix 一致，如 lab_equipment */
    private String objectPrefix = "lab_equipment";

    private long maxFileSize = 5 * 1024 * 1024L;

    public String getBucket() {
        return StringUtils.trimToEmpty(bucketName);
    }

    /** 学生身份证明对象键前缀：{objectPrefix}/student_identity/ */
    public String getStudentIdentityPrefix() {
        String base = StringUtils.defaultIfBlank(objectPrefix, "lab_equipment").trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/student_identity/";
    }
}
