package com.haoyou.service.creator.dto;

/** 创作者申请请求 */
public class ApplyRequest {

    private String reason;
    private String qualification;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
}
