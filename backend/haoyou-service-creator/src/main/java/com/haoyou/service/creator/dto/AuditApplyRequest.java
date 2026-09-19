package com.haoyou.service.creator.dto;

/** 创作者申请审核请求 */
public class AuditApplyRequest {

    /** 1 通过 2 驳回 */
    private Integer action;
    private String remark;

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
