package com.telecom.repository;

import com.telecom.entity.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {
    Optional<AnalysisJob> findByJobId(String jobId);
    List<AnalysisJob> findByOrderByStartTimeDesc();
    long countByStatus(String status);
}
