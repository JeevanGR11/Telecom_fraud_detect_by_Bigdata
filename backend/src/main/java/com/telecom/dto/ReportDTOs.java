package com.telecom.dto;

public class ReportDTOs {

    public static class ReportGenerateRequest {
        private String title;
        private String reportType = "FULL_FRAUD_AUDIT"; // FULL_FRAUD_AUDIT, HIGH_RISK_ONLY, WANGIRI_AUDIT, EXECUTIVE_SUMMARY
        private String startDate;
        private String endDate;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
}
