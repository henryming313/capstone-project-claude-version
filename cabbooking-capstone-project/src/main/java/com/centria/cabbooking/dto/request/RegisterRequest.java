package com.centria.cabbooking.dto.request;

import com.centria.cabbooking.common.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空 / username is required")
    @Size(min = 3, max = 50, message = "用户名长度需在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空 / password is required")
    @Size(min = 6, max = 72, message = "密码长度至少为6位")
    private String password;

    @NotNull(message = "角色不能为空 / role is required")
    private Role role;

    @Pattern(regexp = "^[0-9+\\-\\s]{6,20}$", message = "手机号格式不正确")
    private String phone;
}
