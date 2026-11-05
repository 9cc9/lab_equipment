package org.uestc.weglas.biz.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class StudentLoginRequest {
    @NotBlank(message = "请输入学号")
    private String studentNo;
    @NotBlank(message = "请输入手机号")
    private String phone;
    private String name;
}
