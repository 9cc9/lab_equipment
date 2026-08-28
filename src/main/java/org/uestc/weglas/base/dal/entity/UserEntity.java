package org.uestc.weglas.base.dal.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserEntity {
    private String id;
    private String username;
    private String name;
    private String password;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
