package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.domain.TimeOffStatus;
import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "time_offs")
@Getter
@Setter
@NoArgsConstructor
public class TimeOffJpaEntity extends BaseJpaEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "resource_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID resourceId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeOffStatus status;
}
