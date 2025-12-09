package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.PolicyBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyBindingRepository extends JpaRepository<PolicyBinding, Long> {
    List<PolicyBinding> findByResourceType(String resourceType);

    Optional<PolicyBinding> findByResourceTypeAndContext(String resourceType, String context);

    List<PolicyBinding> findByResourceTypeIn(List<String> resourceTypes);
}
