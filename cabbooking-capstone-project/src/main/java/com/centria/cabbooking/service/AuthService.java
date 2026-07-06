package com.centria.cabbooking.service;

import com.centria.cabbooking.common.enums.AccountStatus;
import com.centria.cabbooking.dto.request.LoginRequest;
import com.centria.cabbooking.dto.request.RegisterRequest;
import com.centria.cabbooking.dto.response.UserResponse;
import com.centria.cabbooking.entity.User;
import com.centria.cabbooking.exception.DuplicateResourceException;
import com.centria.cabbooking.exception.ResourceNotFoundException;
import com.centria.cabbooking.exception.UnauthorizedActionException;
import com.centria.cabbooking.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MVP auth: no sessions, no JWT. The frontend logs in once, receives a
 * {@link UserResponse}, and stores id/role/status in the browser for the
 * lifetime of the tab, passing the userId back on every later request so
 * the service layer can re-check role and account status server-side.
 * (JWT-based auth is listed in the report as a future improvement, not
 * part of the original MVP.)
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("用户名已被占用: " + request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("手机号已被注册: " + request.getPhone());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(AccountStatus.ACTIVE);
        user.setPhone(request.getPhone());

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("用户名或密码错误");
        }
        if (user.getStatus() == AccountStatus.BANNED) {
            throw new UnauthorizedActionException("该账号已被封禁，请联系管理员");
        }
        return UserResponse.from(user);
    }
}
