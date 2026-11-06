package org.uestc.weglas.core.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.uestc.weglas.base.util.exception.AssertUtil;
import org.uestc.weglas.core.config.OssProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AliyunOssService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    ));

    private final OssProperties properties;

    public AliyunOssService(OssProperties properties) {
        this.properties = properties;
    }

    public String uploadStudentIdentityProof(String studentNo, MultipartFile file) {
        assertConfigured();
        AssertUtil.notNull(file, "请选择图片");
        AssertUtil.isTrue(!file.isEmpty(), "请选择图片");
        AssertUtil.isTrue(file.getSize() <= properties.getMaxFileSize(), "图片大小不能超过 5MB");

        String contentType = StringUtils.lowerCase(StringUtils.trimToEmpty(file.getContentType()));
        AssertUtil.isTrue(ALLOWED_CONTENT_TYPES.contains(contentType), "仅支持 jpg、png、webp、gif 图片");

        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String safeStudentNo = sanitizeStudentNo(studentNo);
        String objectKey = normalizePrefix(properties.getStudentIdentityPrefix()) + safeStudentNo + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS client = buildClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);
            client.putObject(properties.getBucket(), objectKey, file.getInputStream(), metadata);
            client.setObjectAcl(properties.getBucket(), objectKey, CannedAccessControlList.PublicRead);
        } catch (IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        } finally {
            client.shutdown();
        }
        return buildPublicUrl(objectKey);
    }

    public boolean isValidStoredUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        String trimmed = url.trim();
        String prefix = buildPublicUrl(normalizePrefix(properties.getStudentIdentityPrefix()));
        return trimmed.startsWith(prefix);
    }

    private void assertConfigured() {
        AssertUtil.isTrue(properties.isEnabled(), "OSS 未启用");
        AssertUtil.notBlank(properties.getAccessKeyId(), "未配置 OSS AccessKey，请联系管理员");
        AssertUtil.notBlank(properties.getAccessKeySecret(), "未配置 OSS AccessKey，请联系管理员");
        AssertUtil.notBlank(properties.getBucket(), "未配置 OSS Bucket");
        AssertUtil.notBlank(properties.getEndpoint(), "未配置 OSS Endpoint");
    }

    private OSS buildClient() {
        return new OSSClientBuilder().build(
                normalizeEndpoint(properties.getEndpoint()),
                properties.getAccessKeyId().trim(),
                properties.getAccessKeySecret().trim());
    }

    private String normalizeEndpoint(String endpoint) {
        String e = endpoint.trim();
        if (e.startsWith("https://")) {
            return e.substring("https://".length());
        }
        if (e.startsWith("http://")) {
            return e.substring("http://".length());
        }
        return e;
    }

    private String normalizePrefix(String prefix) {
        String p = StringUtils.defaultIfBlank(prefix, "lab_equipment/student_identity/");
        if (!p.endsWith("/")) {
            p = p + "/";
        }
        return p;
    }

    private String buildPublicUrl(String objectKey) {
        String domain = StringUtils.trimToNull(properties.getDomain());
        if (domain != null) {
            if (domain.startsWith("http://") || domain.startsWith("https://")) {
                String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
                return base + "/" + objectKey;
            }
            return "https://" + domain + "/" + objectKey;
        }
        return "https://" + properties.getBucket().trim() + "."
                + normalizeEndpoint(properties.getEndpoint()) + "/" + objectKey;
    }

    private static String sanitizeStudentNo(String studentNo) {
        String raw = StringUtils.trimToEmpty(studentNo);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                sb.append(c);
            }
        }
        if (sb.length() == 0) {
            return "unknown";
        }
        return sb.toString();
    }

    private static String resolveExtension(String originalFilename, String contentType) {
        if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.(jpe?g|png|webp|gif)")) {
                return ext;
            }
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        if ("image/gif".equals(contentType)) {
            return ".gif";
        }
        return ".jpg";
    }
}
