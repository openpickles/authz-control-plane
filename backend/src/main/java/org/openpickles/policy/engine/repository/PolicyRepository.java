package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
        Optional<Policy> findByName(String name);

        Optional<Policy> findByNameAndServiceOwnerAndOrigin(
                        String name, String serviceOwner,
                        org.openpickles.policy.engine.model.Policy.PolicyOrigin origin);

        List<Policy> findByNameIn(java.util.Collection<String> names);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(p) FROM Policy p WHERE p.serviceOwner = :serviceOwner AND p.origin = :origin")
        long countByServiceOwnerAndOrigin(
                        @org.springframework.data.repository.query.Param("serviceOwner") String serviceOwner,
                        @org.springframework.data.repository.query.Param("origin") Policy.PolicyOrigin origin);

        org.springframework.data.domain.Page<Policy> findByNameContainingIgnoreCase(String name,
                        org.springframework.data.domain.Pageable pageable);
}
