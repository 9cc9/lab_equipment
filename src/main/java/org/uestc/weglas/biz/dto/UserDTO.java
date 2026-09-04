package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private String id;
    private String username;
    private String name;
    /** ADMIN 管理员，ASSISTANT 学生助管 */
    private String userType;
}
