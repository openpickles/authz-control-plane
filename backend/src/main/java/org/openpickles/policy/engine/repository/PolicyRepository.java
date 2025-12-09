package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByName(String name);

    List<Policy> findByNameIn(java.util.Collection<String> names);
}
