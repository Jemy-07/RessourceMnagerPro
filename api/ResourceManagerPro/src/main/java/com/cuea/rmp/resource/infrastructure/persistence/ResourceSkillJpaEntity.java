package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/** Join entity linking a resource to a skill at a proficiency. Child of the Resource aggregate. */
@Entity
@Table(name = "resource_skills")
@Getter
@Setter
@NoArgsConstructor
public class ResourceSkillJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceJpaEntity resource;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "skill_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID skillId;

    @Column(name = "proficiency", nullable = false)
    private int proficiency;
}
