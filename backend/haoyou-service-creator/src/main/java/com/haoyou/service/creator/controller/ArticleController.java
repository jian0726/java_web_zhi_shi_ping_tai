package com.haoyou.service.creator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.creator.entity.Knowledge;
import com.haoyou.service.creator.entity.KnowledgeBase;
import com.haoyou.service.creator.entity.KnowledgeModule;
import com.haoyou.service.creator.mapper.KnowledgeBaseMapper;
import com.haoyou.service.creator.mapper.KnowledgeMapper;
import com.haoyou.service.creator.mapper.KnowledgeModuleMapper;
import com.haoyou.service.creator.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 创作者端：模块下文章管理（仅知识库所有者可操作）
 */
@RestController
@RequestMapping("/api/creator/base/{baseId}")
public class ArticleController {

    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeModuleMapper moduleMapper;
    private final KnowledgeBaseMapper baseMapper;
    private final JwtUtil jwtUtil;

    public ArticleController(KnowledgeMapper knowledgeMapper,
                             KnowledgeModuleMapper moduleMapper,
                             KnowledgeBaseMapper baseMapper,
                             JwtUtil jwtUtil) {
        this.knowledgeMapper = knowledgeMapper;
        this.moduleMapper = moduleMapper;
        this.baseMapper = baseMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 知识库下全部文章（含模块归属，前端按模块分组） */
    @GetMapping("/articles")
    public Result<List<Map<String, Object>>> list(@RequestHeader("Authorization") String auth,
                                                  @PathVariable Long baseId) {
        requireOwner(auth, baseId);
        List<Knowledge> articles = knowledgeMapper.selectList(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getBaseId, baseId)
                .orderByAsc(Knowledge::getId));
        List<Map<String, Object>> list = articles.stream().map(k -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", k.getId());
            m.put("moduleId", k.getModuleId());
            m.put("title", k.getTitle());
            m.put("summary", k.getSummary());
            m.put("updatedAt", k.getUpdatedAt());
            return m;
        }).toList();
        return Result.success(list);
    }

    /** 单篇文章（编辑回填，含正文） */
    @GetMapping("/article/{articleId}")
    public Result<Map<String, Object>> detail(@RequestHeader("Authorization") String auth,
                                              @PathVariable Long baseId,
                                              @PathVariable Long articleId) {
        requireOwner(auth, baseId);
        Map<String, Object> row = knowledgeMapper.selectArticleDetail(articleId);
        if (row == null || !String.valueOf(baseId).equals(String.valueOf(row.get("base_id")))) {
            throw new BusinessException(404, "文章不存在");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", row.get("id"));
        detail.put("moduleId", row.get("module_id"));
        detail.put("title", row.get("title"));
        detail.put("summary", row.get("summary"));
        detail.put("content", row.get("content"));
        return Result.success(detail);
    }

    /** 创建文章（挂在指定模块下） */
    @PostMapping("/article")
    public Result<Long> create(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @RequestBody Map<String, Object> body) {
        Long userId = requireOwner(auth, baseId);
        Long moduleId = Long.valueOf(String.valueOf(body.get("moduleId")));
        String title = body.get("title") == null ? "" : String.valueOf(body.get("title")).trim();
        if (title.isEmpty()) {
            throw new BusinessException(400, "文章标题不能为空");
        }
        KnowledgeModule m = moduleMapper.selectById(moduleId);
        if (m == null || !m.getBaseId().equals(baseId)) {
            throw new BusinessException(400, "模块不属于该知识库");
        }
        Knowledge k = new Knowledge();
        k.setBaseId(baseId);
        k.setModuleId(moduleId);
        k.setTitle(title);
        k.setSummary(body.get("summary") == null ? "" : String.valueOf(body.get("summary")));
        k.setContent(body.get("content") == null ? "" : String.valueOf(body.get("content")));
        k.setAuthorId(userId);
        // 文章随知识库发布，自身不再单独走审核
        k.setStatus(2);
        k.setCurrentVersion(1);
        knowledgeMapper.insert(k);
        return Result.success(k.getId());
    }

    /** 编辑文章 */
    @PutMapping("/article/{articleId}")
    public Result<Void> update(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @PathVariable Long articleId,
                               @RequestBody Map<String, Object> body) {
        requireOwner(auth, baseId);
        Knowledge k = knowledgeMapper.selectById(articleId);
        if (k == null || !baseId.equals(k.getBaseId())) {
            throw new BusinessException(404, "文章不存在");
        }
        if (body.get("moduleId") != null) {
            Long moduleId = Long.valueOf(String.valueOf(body.get("moduleId")));
            KnowledgeModule m = moduleMapper.selectById(moduleId);
            if (m == null || !m.getBaseId().equals(baseId)) {
                throw new BusinessException(400, "目标模块不属于该知识库");
            }
            k.setModuleId(moduleId);
        }
        if (body.get("title") != null) k.setTitle(String.valueOf(body.get("title")).trim());
        if (body.get("summary") != null) k.setSummary(String.valueOf(body.get("summary")));
        if (body.get("content") != null) k.setContent(String.valueOf(body.get("content")));
        k.setCurrentVersion(k.getCurrentVersion() + 1);
        knowledgeMapper.updateById(k);
        return Result.success();
    }

    /** 删除文章 */
    @DeleteMapping("/article/{articleId}")
    public Result<Void> delete(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @PathVariable Long articleId) {
        requireOwner(auth, baseId);
        Knowledge k = knowledgeMapper.selectById(articleId);
        if (k == null || !baseId.equals(k.getBaseId())) {
            return Result.success();
        }
        knowledgeMapper.deleteById(articleId);
        return Result.success();
    }

    private Long requireOwner(String auth, Long baseId) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        Long userId;
        try {
            userId = jwtUtil.parseUserId(auth.substring(7));
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        KnowledgeBase base = baseMapper.selectById(baseId);
        if (base == null) {
            throw new BusinessException(404, "知识库不存在");
        }
        if (!userId.equals(base.getAuthorId())) {
            throw new BusinessException(403, "仅知识库所有者可操作");
        }
        return userId;
    }
}
