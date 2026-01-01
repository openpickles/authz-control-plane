package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.PolicyBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyBundleRepository extends JpaRepository<PolicyBundle, Long> {
    org.springframework.data.domain.Page<PolicyBundle> findByNameContainingIgnoreCase(String name,
            org.springframework.data.domain.Pageable pageable);
}
