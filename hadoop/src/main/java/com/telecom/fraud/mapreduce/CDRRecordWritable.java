package com.telecom.fraud.mapreduce;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Custom Hadoop Writable representing an extracted CDR event emitted by the Mapper.
 */
public class CDRRecordWritable implements Writable {

    private String receiverNumber;
    private int duration;
    private String callTime;
    private String callType;
    private boolean nightCall;

    public CDRRecordWritable() {
        this.receiverNumber = "";
        this.duration = 0;
        this.callTime = "";
        this.callType = "";
        this.nightCall = false;
    }

    public CDRRecordWritable(String receiverNumber, int duration, String callTime, String callType, boolean nightCall) {
        this.receiverNumber = receiverNumber != null ? receiverNumber : "";
        this.duration = duration;
        this.callTime = callTime != null ? callTime : "";
        this.callType = callType != null ? callType : "";
        this.nightCall = nightCall;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(receiverNumber);
        out.writeInt(duration);
        out.writeUTF(callTime);
        out.writeUTF(callType);
        out.writeBoolean(nightCall);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.receiverNumber = in.readUTF();
        this.duration = in.readInt();
        this.callTime = in.readUTF();
        this.callType = in.readUTF();
        this.nightCall = in.readBoolean();
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }

    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public boolean isNightCall() {
        return nightCall;
    }

    public void setNightCall(boolean nightCall) {
        this.nightCall = nightCall;
    }

    @Override
    public String toString() {
        return receiverNumber + "," + duration + "," + callTime + "," + callType + "," + nightCall;
    }
}
