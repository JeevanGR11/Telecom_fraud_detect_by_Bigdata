package com.telecom.dto;

import java.util.List;

public class DatasetDTOs {

    public static class DatasetGenerateRequest {
        private String name;
        private int count = 1000;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class DatasetPreviewDTO {
        private String name;
        private int totalRecords;
        private List<String> headers;
        private List<List<String>> sampleRows;
        private boolean isValid;
        private List<String> validationErrors;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public List<String> getHeaders() { return headers; }
        public void setHeaders(List<String> headers) { this.headers = headers; }
        public List<List<String>> getSampleRows() { return sampleRows; }
        public void setSampleRows(List<List<String>> sampleRows) { this.sampleRows = sampleRows; }
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        public List<String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }
    }
}
