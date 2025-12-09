package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.ResourceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceProviderRepository extends JpaRepository<ResourceProvider, Long> {
    List<ResourceProvider> findByResourceType(String resourceType);
}
