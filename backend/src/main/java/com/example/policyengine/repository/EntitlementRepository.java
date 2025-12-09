package com.example.policyengine.repository;

import com.example.policyengine.model.Entitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, Long> {
    // Find entitlements that contain the specific resourceId in their set
    List<Entitlement> findByResourceTypeAndResourceIdsContaining(String resourceType, String resourceId);

    List<Entitlement> findBySubjectTypeAndSubjectId(Entitlement.SubjectType subjectType, String subjectId);

    List<Entitlement> findByResourceTypeAndSubjectTypeAndSubjectId(String resourceType,
            Entitlement.SubjectType subjectType, String subjectId);
}
