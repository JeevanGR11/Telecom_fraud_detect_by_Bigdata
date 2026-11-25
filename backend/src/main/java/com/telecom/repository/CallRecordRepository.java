package com.telecom.repository;

import com.telecom.entity.CallRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {

    List<CallRecord> findByCallerNumberOrderByCallTimestampDesc(String callerNumber);

    List<CallRecord> findByReceiverNumberOrderByCallTimestampDesc(String receiverNumber);

    @Query("SELECT c FROM CallRecord c WHERE " +
           "(:caller IS NULL OR c.callerNumber LIKE %:caller%) AND " +
           "(:receiver IS NULL OR c.receiverNumber LIKE %:receiver%) AND " +
           "(:callType IS NULL OR c.callType = :callType) AND " +
           "(:startDate IS NULL OR c.callTimestamp >= :startDate) AND " +
           "(:endDate IS NULL OR c.callTimestamp <= :endDate)")
    Page<CallRecord> searchRecords(
            @Param("caller") String caller,
            @Param("receiver") String receiver,
            @Param("callType") String callType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByCallerNumber(String callerNumber);
    long countByReceiverNumber(String receiverNumber);

    @Query("SELECT COUNT(DISTINCT c.receiverNumber) FROM CallRecord c WHERE c.callerNumber = :caller")
    long countDistinctReceiversByCaller(@Param("caller") String caller);

    @Query("SELECT COALESCE(SUM(c.callDuration), 0) FROM CallRecord c WHERE c.callerNumber = :caller")
    long sumDurationByCaller(@Param("caller") String caller);

    @Query("SELECT c.callType, COUNT(c) FROM CallRecord c GROUP BY c.callType")
    List<Object[]> getCallTypeDistribution();

    @Query("SELECT c.sourceRegion, COUNT(c) FROM CallRecord c WHERE c.sourceRegion IS NOT NULL GROUP BY c.sourceRegion")
    List<Object[]> getRegionDistribution();
}
