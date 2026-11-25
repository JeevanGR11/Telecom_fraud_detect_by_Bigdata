package com.telecom.service;

import com.telecom.repository.CallRecordRepository;
import com.telecom.repository.FraudResultRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeoAnalysisService {

    private final CallRecordRepository callRecordRepository;
    private final FraudResultRepository fraudResultRepository;

    public GeoAnalysisService(CallRecordRepository callRecordRepository,
                              FraudResultRepository fraudResultRepository) {
        this.callRecordRepository = callRecordRepository;
        this.fraudResultRepository = fraudResultRepository;
    }

    public Map<String, Object> getGeoAnalytics() {
        Map<String, Object> result = new HashMap<>();

        // Indian State-level distribution
        List<Map<String, Object>> stateData = new ArrayList<>();
        stateData.add(createStateMap("Karnataka", "Bengaluru, Mysuru", 380, 26, 85, "CRITICAL (Wangiri Hub)"));
        stateData.add(createStateMap("Delhi NCR", "Delhi, Noida, Gurugram", 290, 31, 70, "HIGH (Robocall Dialers)"));
        stateData.add(createStateMap("Maharashtra", "Mumbai, Pune", 240, 8, 60, "MEDIUM (Toll Bypass)"));
        stateData.add(createStateMap("Tamil Nadu", "Chennai, Coimbatore", 185, 4, 15, "LOW (Normal Patterns)"));
        stateData.add(createStateMap("Telangana", "Hyderabad", 160, 3, 10, "LOW (Normal Patterns)"));
        stateData.add(createStateMap("West Bengal", "Kolkata", 95, 2, 8, "NORMAL (Benign Subscribers)"));
        stateData.add(createStateMap("Gujarat", "Ahmedabad, Surat", 85, 2, 5, "NORMAL (Benign Subscribers)"));
        stateData.add(createStateMap("Kerala", "Kochi, Thiruvananthapuram", 75, 1, 5, "NORMAL (Benign Subscribers)"));

        // Top Regional Hotspots
        List<Map<String, Object>> hotspots = new ArrayList<>();
        hotspots.add(createHotspot("Bengaluru Central Circle", "Karnataka", "103.24.18.52", "Wangiri Automated Blast", 25));
        hotspots.add(createHotspot("North Delhi Gateway", "Delhi", "49.207.12.90", "Robocall High-Rate Outbound", 30));
        hotspots.add(createHotspot("Mumbai South Trunk", "Maharashtra", "122.161.45.10", "PBX Toll Bypass", 8));

        result.put("stateDistribution", stateData);
        result.put("hotspots", hotspots);
        result.put("totalCirclesMonitored", 14);
        result.put("highestRiskCircle", "Bengaluru, Karnataka");

        return result;
    }

    private Map<String, Object> createStateMap(String state, String cities, int totalCalls, int suspiciousCalls, int maxRisk, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("cities", cities);
        map.put("totalCalls", totalCalls);
        map.put("suspiciousCalls", suspiciousCalls);
        map.put("maxRiskScore", maxRisk);
        map.put("threatStatus", status);
        return map;
    }

    private Map<String, Object> createHotspot(String circle, String state, String ip, String pattern, int incidents) {
        Map<String, Object> map = new HashMap<>();
        map.put("circleName", circle);
        map.put("state", state);
        map.put("sourceIp", ip);
        map.put("pattern", pattern);
        map.put("incidentCount", incidents);
        return map;
    }
}
