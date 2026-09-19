package com.haoyou.service.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 岗位认知 job_cognition
 */
@TableName("job_cognition")
public class JobCognition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jobName;
    private String jobDuty;
    private String jobRequire;
    private String abilityModel;
    private String workScene;
    /** 1 启用 0 停用 */
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobDuty() { return jobDuty; }
    public void setJobDuty(String jobDuty) { this.jobDuty = jobDuty; }
    public String getJobRequire() { return jobRequire; }
    public void setJobRequire(String jobRequire) { this.jobRequire = jobRequire; }
    public String getAbilityModel() { return abilityModel; }
    public void setAbilityModel(String abilityModel) { this.abilityModel = abilityModel; }
    public String getWorkScene() { return workScene; }
    public void setWorkScene(String workScene) { this.workScene = workScene; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
