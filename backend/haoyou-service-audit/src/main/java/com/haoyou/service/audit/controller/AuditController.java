package com.haoyou.service.audit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.common.ResultCode;
import com.haoyou.service.audit.entity.KnowledgeAuditRecord;
import com.haoyou.service.audit.entity.KnowledgeBase;
import com.haoyou.service.audit.entity.NotifyMessage;
import com.haoyou.service.audit.mapper.KnowledgeAuditRecordMapper;
import com.haoyou.service.audit.mapper.KnowledgeBaseMapper;
import com.haoyou.service.audit.mapper.NotifyMessageMapper;
import com.haoyou.service.audit.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核者端接口（走网关 /api/audit/** 路由，直连 59854 亦可）
 * 审核对象：知识库 knowledge_base（模块与文章随库发布）
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeAuditRecordMapper recordMapper;
    private final NotifyMessageMapper notifyMapper;
    private final JwtUtil jwtUtil;

    public AuditController(KnowledgeBaseMapper baseMapper,
                           KnowledgeAuditRecordMapper recordMapper,
                           NotifyMessageMapper notifyMapper,
                           JwtUtil jwtUtil) {
        this.baseMapper = baseMapper;
        this.recordMapper = recordMapper;
        this.notifyMapper = notifyMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 审核分页（status 默认 1 待审） */
    @GetMapping("/pending")
    public Result<Map<String, Object>> pending(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Integer st = status == null ? 1 : status;
        long total = baseMapper.countAuditPage(st);
        List<Map<String, Object>> list = total == 0 ? List.of()
                : baseMapper.selectAuditPage(st, (page - 1) * size, (int) size);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("list", list);
        return Result.success(data);
    }

    /** 审核详情 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> row = baseMapper.selectDetail(id);
        if (row == null) {
            throw new BusinessException(404, "知识库不存在");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", row.get("id"));
        detail.put("kbNo", row.get("kb_no"));
        detail.put("name", row.get("name"));
        detail.put("kbType", row.get("kb_type"));
        detail.put("summary", row.get("summary"));
        detail.put("intro", row.get("intro"));
        detail.put("status", row.get("status"));
        detail.put("currentVersion", row.get("current_version"));
        detail.put("authorName", row.get("author_name"));
        detail.put("updatedAt", row.get("updated_at"));
        return Result.success(detail);
    }

    /** 审核动作：action 1 通过 2 驳回；驳回必填理由 */
    @PutMapping("/{id}/action")
    public Result<Void> action(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        Long auditorId = requireUserId(auth);
        Integer action;
        try {
            action = Integer.valueOf(String.valueOf(body.get("action")));
        } catch (Exception e) {
            throw new BusinessException(400, "action 必须为 1(通过) 或 2(驳回)");
        }
        String remark = body.get("remark") == null ? "" : String.valueOf(body.get("remark"));
        if (action != 1 && action != 2) {
            throw new BusinessException(400, "action 必须为 1(通过) 或 2(驳回)");
        }
        if (action == 2 && remark.isBlank()) {
            throw new BusinessException(400, "驳回必须填写理由");
        }
        KnowledgeBase base = baseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "知识库不存在");
        }
        if (base.getStatus() == null || base.getStatus() != 1) {
            throw new BusinessException(400, "仅待审知识库可审核");
        }

        base.setStatus(action == 1 ? 2 : 3);
        if (action == 1) {
            base.setPublishedAt(LocalDateTime.now());
        }
        baseMapper.updateById(base);

        KnowledgeAuditRecord record = new KnowledgeAuditRecord();
        record.setKnowledgeId(id);
        record.setVersion(base.getCurrentVersion());
        record.setAction(action);
        record.setRemark(remark);
        record.setAuditorId(auditorId);
        record.setAuditedAt(LocalDateTime.now());
        recordMapper.insert(record);

        NotifyMessage msg = new NotifyMessage();
        msg.setUserId(base.getAuthorId());
        msg.setTitle(action == 1 ? "您的知识库已审核通过" : "您的知识库被驳回");
        msg.setContent(action == 1
                ? "知识库《" + base.getName() + "》已审核通过并发布。"
                : "知识库《" + base.getName() + "》未通过审核，理由：" + remark);
        msg.setBizType(action == 1 ? "APPROVE" : "REJECT");
        msg.setBizId(id);
        msg.setIsRead(0);
        notifyMapper.insert(msg);
        return Result.success();
    }

    /** 审核历史 */
    @GetMapping("/records/{baseId}")
    public Result<List<KnowledgeAuditRecord>> records(@PathVariable Long baseId) {
        return Result.success(recordMapper.selectList(new LambdaQueryWrapper<KnowledgeAuditRecord>()
                .eq(KnowledgeAuditRecord::getKnowledgeId, baseId)
                .orderByDesc(KnowledgeAuditRecord::getAuditedAt)));
    }

    private Long requireUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        try {
            return jwtUtil.parseUserId(auth.substring(7));
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }
}
