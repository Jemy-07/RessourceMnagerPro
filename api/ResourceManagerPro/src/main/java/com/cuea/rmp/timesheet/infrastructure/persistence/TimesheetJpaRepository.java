package com.cuea.rmp.timesheet.infrastructure.persistence;

import com.cuea.rmp.timesheet.domain.TimesheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimesheetJpaRepository extends JpaRepository<TimesheetJpaEntity, UUID> {

    Optional<TimesheetJpaEntity> findByIdAndDeletedFalse(UUID id);

    List<TimesheetJpaEntity> findByResourceIdAndWorkDateBetweenAndDeletedFalseOrderByWorkDateAsc(
            UUID resourceId, LocalDate from, LocalDate to);

    List<TimesheetJpaEntity> findByAssignmentIdAndStatusAndDeletedFalse(UUID assignmentId, TimesheetStatus status);
}
