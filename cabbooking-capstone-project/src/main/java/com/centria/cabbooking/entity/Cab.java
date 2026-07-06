package com.centria.cabbooking.entity;

import com.centria.cabbooking.common.enums.CabStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cabs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false, length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CabStatus status = CabStatus.AVAILABLE;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = CabStatus.AVAILABLE;
        }
    }
}
