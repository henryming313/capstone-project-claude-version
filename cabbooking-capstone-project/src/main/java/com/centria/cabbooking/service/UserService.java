package com.centria.cabbooking.service;

import com.centria.cabbooking.common.enums.AccountStatus;
import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.dto.response.UserResponse;
import com.centria.cabbooking.entity.User;
import com.centria.cabbooking.exception.ResourceNotFoundException;
import com.centria.cabbooking.exception.UnauthorizedActionException;
import com.centria.cabbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: id=" + id));
    }

    /** Confirms the given user exists, is ACTIVE, and holds the expected role. Used as the
     *  backend-level permission gate before any role-sensitive action. */
    public User requireActiveUserWithRole(Long userId, Role expectedRole) {
        User user = getUserOrThrow(userId);
        if (user.getStatus() == AccountStatus.BANNED) {
            throw new UnauthorizedActionException("账号已被封禁，无法执行该操作");
        }
        if (user.getRole() != expectedRole) {
            throw new UnauthorizedActionException("该操作需要 " + expectedRole + " 权限");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setStatus(Long userId, AccountStatus status) {
        User user = getUserOrThrow(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new UnauthorizedActionException("不能封禁管理员账号");
        }
        user.setStatus(status);
        return UserResponse.from(userRepository.save(user));
    }
}
