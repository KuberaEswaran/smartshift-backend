package com.smartshift.smartshift_backend.organization.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "organization_settings")
public class OrganizationSettings {

    @Id
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "proximity_weight", precision = 19, scale = 6)
    private BigDecimal proximityWeight;

    @Column(name = "skill_match_weight", precision = 19, scale = 6)
    private BigDecimal skillMatchWeight;

    @Column(name = "overtime_risk_weight", precision = 19, scale = 6)
    private BigDecimal overtimeRiskWeight;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}