package org.openpickles.policy.engine.repository;

import org.openpickles.policy.engine.model.ServiceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ServiceRegistryRepository extends JpaRepository<ServiceRegistry, Long> {

    Optional<ServiceRegistry> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ServiceRegistry s WHERE s.name = :name")
    Optional<ServiceRegistry> findByNameForUpdate(@Param("name") String name);
}
