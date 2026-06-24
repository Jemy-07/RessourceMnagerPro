package com.cuea.rmp.request.infrastructure.persistence;

import com.cuea.rmp.request.domain.RequestStatus;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
public class RequestJpaEntity extends BaseJpaEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "requester_id", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private UUID requesterId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "approver_id", length = 36, columnDefinition = "CHAR(36)")
    private UUID approverId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "resource_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID resourceId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "project_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID projectId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "allocation_pct", nullable = false)
    private int allocationPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
