package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

    Optional<ResourceType> findByKey(String key);

    Page<ResourceType> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Support for finding by key if needed for validation
    boolean existsByKey(String key);
}
