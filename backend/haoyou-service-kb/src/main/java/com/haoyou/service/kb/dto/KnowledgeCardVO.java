package com.haoyou.service.kb.dto;

import java.time.LocalDateTime;

/** 知识卡片（列表/推荐/目录通用） */
public class KnowledgeCardVO {

    private Long id;
    private String title;
    private String kbType;
    private String summary;
    private String coverUrl;
    private String authorName;
    private LocalDateTime publishedAt;
    private Integer subscribeCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getKbType() { return kbType; }
    public void setKbType(String kbType) { this.kbType = kbType; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Integer getSubscribeCount() { return subscribeCount; }
    public void setSubscribeCount(Integer subscribeCount) { this.subscribeCount = subscribeCount; }
}
