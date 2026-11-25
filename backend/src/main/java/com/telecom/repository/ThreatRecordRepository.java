package com.telecom.repository;

import com.telecom.entity.ThreatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreatRecordRepository extends JpaRepository<ThreatRecord, Long> {
    Optional<ThreatRecord> findByIpAddress(String ipAddress);
    List<ThreatRecord> findByOrderByDetectedAtDesc();
    List<ThreatRecord> findByThreatLevel(String threatLevel);
}
