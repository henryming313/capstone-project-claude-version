package com.centria.cabbooking.entity;

import com.centria.cabbooking.common.enums.AccountStatus;
import com.centria.cabbooking.common.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt hash - never the plaintext password. */
    @Column(nullable = false, length = 100)
    private String password;

    // @JdbcTypeCode(VARCHAR) forces a plain VARCHAR column instead of a
    // native MySQL ENUM type. Hibernate 6's default enum mapping on MySQL
    // uses a native ENUM(...) column, which is fragile to ALTER once rows
    // exist (adding/renaming/reordering values can trigger "Data truncated"
    // errors on schema update) - VARCHAR sidesteps that entirely.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }
}
