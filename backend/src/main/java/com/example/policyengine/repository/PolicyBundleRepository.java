package com.example.policyengine.repository;

import com.example.policyengine.model.PolicyBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyBundleRepository extends JpaRepository<PolicyBundle, Long> {
}
