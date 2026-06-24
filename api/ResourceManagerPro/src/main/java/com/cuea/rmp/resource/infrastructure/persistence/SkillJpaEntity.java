package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
public class SkillJpaEntity extends BaseJpaEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "org_id", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
