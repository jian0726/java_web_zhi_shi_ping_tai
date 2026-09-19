package com.haoyou.service.creator.dto;

/** 创建/编辑知识库文章请求 */
public class KnowledgeUpsertRequest {

    private String title;
    private String kbType;
    private Long jobId;
    private String summary;
    private String displayType;
    private String coverUrl;
    private String content;
    /** false=存草稿(0) true=提交审核(1)，默认 false */
    private boolean submit;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getKbType() { return kbType; }
    public void setKbType(String kbType) { this.kbType = kbType; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDisplayType() { return displayType; }
    public void setDisplayType(String displayType) { this.displayType = displayType; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isSubmit() { return submit; }
    public void setSubmit(boolean submit) { this.submit = submit; }
}
