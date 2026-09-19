package com.haoyou.service.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户表 sys_user
 */
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号 */
    private String phone;

    /** BCrypt 加密后的密码 */
    private String passwordHash;

    private String nickname;

    private String avatar;

    /** 职业 */
    private String occupation;

    /** 工作年限 */
    private String workYears;

    /** 工作状态（在职/自由职业/学生/待业） */
    private String workStatus;

    /** 身份证号 */
    private String idCard;

    /** 工作证明文件 URL */
    private String workProof;

    /** 1 启用 0 禁用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getOccupation() { return occupation; }

    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getWorkYears() { return workYears; }

    public void setWorkYears(String workYears) { this.workYears = workYears; }

    public String getWorkStatus() { return workStatus; }

    public void setWorkStatus(String workStatus) { this.workStatus = workStatus; }

    public String getIdCard() { return idCard; }

    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getWorkProof() { return workProof; }

    public void setWorkProof(String workProof) { this.workProof = workProof; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getDeleted() { return deleted; }

    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
