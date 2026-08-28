package org.uestc.weglas.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String id;
    private String username;
    private String name;
    private String password;
    private String status;
}
