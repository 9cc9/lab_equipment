package org.uestc.weglas.biz.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private UserDTO user;
    private String sessionToken;
}
