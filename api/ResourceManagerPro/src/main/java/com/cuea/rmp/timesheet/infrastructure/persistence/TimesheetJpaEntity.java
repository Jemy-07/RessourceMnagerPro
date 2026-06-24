package com.cuea.rmp.timesheet.infrastructure.persistence;

import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import com.cuea.rmp.timesheet.domain.TimesheetStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
@NoArgsConstructor
public class TimesheetJpaEntity extends BaseJpaEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "resource_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID resourceId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "assignment_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID assignmentId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimesheetStatus status;
}
