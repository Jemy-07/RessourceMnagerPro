package com.cuea.rmp.timesheet.infrastructure.persistence;

import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import com.cuea.rmp.timesheet.domain.Timesheet;
import com.cuea.rmp.timesheet.domain.TimesheetStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TimesheetPersistenceAdapter implements TimesheetRepository {

    private final TimesheetJpaRepository jpaRepository;
    private final TimesheetMapper mapper;

    public TimesheetPersistenceAdapter(TimesheetJpaRepository jpaRepository, TimesheetMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Timesheet save(Timesheet timesheet) {
        TimesheetJpaEntity entity = jpaRepository.findById(timesheet.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, timesheet);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(timesheet));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Timesheet> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Timesheet> findByResourceAndDateRange(UUID resourceId, LocalDate from, LocalDate to) {
        return jpaRepository
                .findByResourceIdAndWorkDateBetweenAndDeletedFalseOrderByWorkDateAsc(resourceId, from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Timesheet> findApprovedByAssignmentId(UUID assignmentId) {
        return jpaRepository
                .findByAssignmentIdAndStatusAndDeletedFalse(assignmentId, TimesheetStatus.APPROVED)
                .stream().map(mapper::toDomain).toList();
    }
}
