package com.telecom.service;

import com.telecom.entity.ThreatRecord;
import com.telecom.repository.ThreatRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IPThreatService {

    private final ThreatRecordRepository threatRecordRepository;

    public IPThreatService(ThreatRecordRepository threatRecordRepository) {
        this.threatRecordRepository = threatRecordRepository;
    }

    public List<ThreatRecord> getAllThreats() {
        return threatRecordRepository.findByOrderByDetectedAtDesc();
    }

    public ThreatRecord addThreat(ThreatRecord record) {
        return threatRecordRepository.save(record);
    }
}
