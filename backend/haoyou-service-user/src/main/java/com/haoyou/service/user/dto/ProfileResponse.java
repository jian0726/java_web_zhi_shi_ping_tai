package com.haoyou.service.user.dto;

import java.time.LocalDateTime;

/** 个人资料（读者端个人中心 / 创作者端个人信息） */
public class ProfileResponse {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;

    /** 创作者认证信息 */
    private String occupation;
    private String workYears;
    private String workStatus;
    private String idCard;
    private String workProof;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
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
}
