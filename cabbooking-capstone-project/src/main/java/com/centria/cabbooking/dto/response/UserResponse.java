package com.centria.cabbooking.dto.response;

import com.centria.cabbooking.common.enums.AccountStatus;
import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** Never expose the password hash - this DTO is what actually leaves the backend. */
@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private AccountStatus status;
    private String phone;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }
}
