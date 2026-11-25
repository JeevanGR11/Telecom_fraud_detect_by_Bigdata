package com.telecom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_records", indexes = {
    @Index(name = "idx_cdr_caller", columnList = "caller_number"),
    @Index(name = "idx_cdr_receiver", columnList = "receiver_number"),
    @Index(name = "idx_cdr_timestamp", columnList = "call_timestamp"),
    @Index(name = "idx_cdr_type", columnList = "call_type")
})
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false, length = 50)
    private String recordId;

    @Column(name = "caller_number", nullable = false, length = 15)
    private String callerNumber;

    @Column(name = "receiver_number", nullable = false, length = 15)
    private String receiverNumber;

    @Column(name = "call_duration", nullable = false)
    private Integer callDuration; // in seconds

    @Column(name = "call_type", nullable = false, length = 20)
    private String callType; // OUTGOING, INCOMING, INTERNATIONAL, MISSED, REJECTED

    @Column(name = "call_timestamp", nullable = false)
    private LocalDateTime callTimestamp;

    @Column(name = "source_region", length = 100)
    private String sourceRegion;

    @Column(name = "destination_region", length = 100)
    private String destinationRegion;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "network_type", length = 20)
    private String networkType = "4G VoLTE";

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public CallRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getCallerNumber() { return callerNumber; }
    public void setCallerNumber(String callerNumber) { this.callerNumber = callerNumber; }

    public String getReceiverNumber() { return receiverNumber; }
    public void setReceiverNumber(String receiverNumber) { this.receiverNumber = receiverNumber; }

    public Integer getCallDuration() { return callDuration; }
    public void setCallDuration(Integer callDuration) { this.callDuration = callDuration; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public LocalDateTime getCallTimestamp() { return callTimestamp; }
    public void setCallTimestamp(LocalDateTime callTimestamp) { this.callTimestamp = callTimestamp; }

    public String getSourceRegion() { return sourceRegion; }
    public void setSourceRegion(String sourceRegion) { this.sourceRegion = sourceRegion; }

    public String getDestinationRegion() { return destinationRegion; }
    public void setDestinationRegion(String destinationRegion) { this.destinationRegion = destinationRegion; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getNetworkType() { return networkType; }
    public void setNetworkType(String networkType) { this.networkType = networkType; }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
