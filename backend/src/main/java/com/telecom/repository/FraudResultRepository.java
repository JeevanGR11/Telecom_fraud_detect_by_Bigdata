package com.telecom.repository;

import com.telecom.entity.FraudResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudResultRepository extends JpaRepository<FraudResult, Long> {

    Optional<FraudResult> findByMobileNumber(String mobileNumber);

    List<FraudResult> findByOrderByRiskScoreDesc(Pageable pageable);

    List<FraudResult> findByRiskLevelOrderByRiskScoreDesc(String riskLevel);

    long countByRiskLevel(String riskLevel);

    @Query("SELECT f.riskLevel, COUNT(f) FROM FraudResult f GROUP BY f.riskLevel")
    List<Object[]> getRiskDistribution();

    @Query("SELECT COUNT(f) FROM FraudResult f WHERE f.riskScore >= 40")
    long countSuspiciousNumbers();
}
