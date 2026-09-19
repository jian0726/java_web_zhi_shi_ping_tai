package com.haoyou.service.creator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.creator.dto.PageVO;
import com.haoyou.service.creator.entity.KnowledgeBase;
import com.haoyou.service.creator.entity.KnowledgeCollaborator;
import com.haoyou.service.creator.mapper.KnowledgeBaseMapper;
import com.haoyou.service.creator.mapper.KnowledgeCollaboratorMapper;
import com.haoyou.service.creator.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 创作者端：知识库管理（走网关 /api/creator/** 路由，直连 59852 亦可）
 */
@RestController
@RequestMapping("/api/creator/base")
public class CreatorBaseController {

    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeCollaboratorMapper collaboratorMapper;
    private final JwtUtil jwtUtil;

    public CreatorBaseController(KnowledgeBaseMapper baseMapper,
                                 KnowledgeCollaboratorMapper collaboratorMapper,
                                 JwtUtil jwtUtil) {
        this.baseMapper = baseMapper;
        this.collaboratorMapper = collaboratorMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 我的知识库分页（status: 0草稿 1待审 2已发布 3驳回，不传查全部） */
    @GetMapping("/page")
    public Result<PageVO<Map<String, Object>>> page(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long userId = requireUserId(auth);
        long total = baseMapper.countMyPage(userId, status);
        List<Map<String, Object>> list = total == 0 ? List.of()
                : baseMapper.selectMyPage(userId, status, (page - 1) * size, (int) size);
        return Result.success(new PageVO<>(total, list));
    }

    /** 知识库详情（仅本人） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@RequestHeader("Authorization") String auth,
                                              @PathVariable Long id) {
        Long userId = requireUserId(auth);
        Map<String, Object> row = baseMapper.selectDetail(id);
        if (row == null) {
            throw new BusinessException(404, "知识库不存在");
        }
        if (!String.valueOf(userId).equals(String.valueOf(row.get("author_id")))) {
            throw new BusinessException(403, "无权查看他人知识库");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", row.get("id"));
        detail.put("kbNo", row.get("kb_no"));
        detail.put("name", row.get("name"));
        detail.put("kbType", row.get("kb_type"));
        detail.put("summary", row.get("summary"));
        detail.put("intro", row.get("intro"));
        detail.put("displayType", row.get("display_type"));
        detail.put("coverUrl", row.get("cover_url"));
        detail.put("status", row.get("status"));
        detail.put("currentVersion", row.get("current_version"));
        detail.put("subscribeCount", row.get("subscribe_count"));
        detail.put("authorName", row.get("author_name"));
        detail.put("publishedAt", row.get("published_at"));
        detail.put("updatedAt", row.get("updated_at"));
        return Result.success(detail);
    }

    /** 创建知识库：submit=false 存草稿，true 提交审核 */
    @PostMapping
    public Result<Long> create(@RequestHeader("Authorization") String auth,
                               @RequestBody Map<String, Object> body) {
        Long userId = requireUserId(auth);
        String name = str(body.get("name"));
        if (name.isBlank()) {
            throw new BusinessException(400, "知识库名称不能为空");
        }
        KnowledgeBase base = new KnowledgeBase();
        apply(base, body);
        base.setKbNo("KB-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));
        base.setAuthorId(userId);
        base.setStatus(Boolean.TRUE.equals(body.get("submit")) ? 1 : 0);
        base.setCurrentVersion(1);
        base.setSubscribeCount(0);
        baseMapper.insert(base);
        // 登记所有者为协作者
        KnowledgeCollaborator owner = new KnowledgeCollaborator();
        owner.setKnowledgeId(base.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        collaboratorMapper.insert(owner);
        return Result.success(base.getId());
    }

    /** 编辑知识库（待审不可改；已发布改后需重新提交？保持：已发布仅允许改展示字段并直接生效，不重新走审核） */
    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        Long userId = requireUserId(auth);
        KnowledgeBase base = requireOwner(userId, id);
        if (base.getStatus() != null && base.getStatus() == 1) {
            throw new BusinessException(400, "审核中的知识库不能编辑");
        }
        apply(base, body);
        if (base.getStatus() != null && base.getStatus() == 3) {
            base.setStatus(Boolean.TRUE.equals(body.get("submit")) ? 1 : 0);
            base.setCurrentVersion(base.getCurrentVersion() + 1);
        }
        baseMapper.updateById(base);
        return Result.success();
    }

    /** 删除知识库（待审不可删） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = requireUserId(auth);
        KnowledgeBase base = requireOwner(userId, id);
        if (base.getStatus() != null && base.getStatus() == 1) {
            throw new BusinessException(400, "审核中的知识库不能删除");
        }
        baseMapper.deleteById(id);
        return Result.success();
    }

    private void apply(KnowledgeBase base, Map<String, Object> body) {
        if (body.get("name") != null) base.setName(str(body.get("name")));
        if (body.get("kbType") != null) base.setKbType(str(body.get("kbType")));
        if (body.get("summary") != null) base.setSummary(str(body.get("summary")));
        if (body.get("intro") != null) base.setIntro(str(body.get("intro")));
        if (body.get("displayType") != null) base.setDisplayType(str(body.get("displayType")));
        if (body.get("coverUrl") != null) base.setCoverUrl(str(body.get("coverUrl")));
    }

    private KnowledgeBase requireOwner(Long userId, Long baseId) {
        KnowledgeBase base = baseMapper.selectById(baseId);
        if (base == null) {
            throw new BusinessException(404, "知识库不存在");
        }
        if (!userId.equals(base.getAuthorId())) {
            throw new BusinessException(403, "仅知识库所有者可操作");
        }
        return base;
    }

    private String str(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }

    private Long requireUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        try {
            return jwtUtil.parseUserId(auth.substring(7));
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }
}
