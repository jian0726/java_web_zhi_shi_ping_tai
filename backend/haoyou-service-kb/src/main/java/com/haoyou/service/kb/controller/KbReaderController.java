package com.haoyou.service.kb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.kb.dto.KnowledgeCardVO;
import com.haoyou.service.kb.dto.PageVO;
import com.haoyou.service.kb.entity.Knowledge;
import com.haoyou.service.kb.entity.KnowledgeBase;
import com.haoyou.service.kb.entity.KnowledgeModule;
import com.haoyou.service.kb.entity.Subscription;
import com.haoyou.service.kb.mapper.KnowledgeBaseMapper;
import com.haoyou.service.kb.mapper.KnowledgeMapper;
import com.haoyou.service.kb.mapper.KnowledgeModuleMapper;
import com.haoyou.service.kb.mapper.SubscriptionMapper;
import com.haoyou.service.kb.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 读者端接口（走网关 /api/kb/** 路由，直连 59853 亦可）
 * 三级结构：知识库 base → 模块 module → 文章 article
 */
@RestController
@RequestMapping("/api/kb")
public class KbReaderController {

    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeModuleMapper moduleMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final JwtUtil jwtUtil;

    public KbReaderController(KnowledgeBaseMapper baseMapper,
                              KnowledgeModuleMapper moduleMapper,
                              KnowledgeMapper knowledgeMapper,
                              SubscriptionMapper subscriptionMapper,
                              JwtUtil jwtUtil) {
        this.baseMapper = baseMapper;
        this.moduleMapper = moduleMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 分类列表（含数量，按知识库统计） */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(baseMapper.selectCategories());
    }

    /** 知识库分页：type 可选；sort=new|hot */
    @GetMapping("/page")
    public Result<PageVO<KnowledgeCardVO>> page(@RequestParam(required = false) String type,
                                                @RequestParam(defaultValue = "new") String sort,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "16") long size) {
        long total = baseMapper.countPage(type);
        List<KnowledgeCardVO> list = total == 0 ? List.of()
                : baseMapper.selectPage(type, sort, (page - 1) * size, (int) size);
        return Result.success(new PageVO<>(total, list));
    }

    /** 知识库详情（含模块-文章树；携带 token 时返回 subscribed） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id,
                                              @RequestHeader(value = "Authorization", required = false) String auth) {
        Map<String, Object> row = baseMapper.selectDetail(id);
        if (row == null) {
            throw new BusinessException(404, "内容不存在或未发布");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", row.get("id"));
        detail.put("kbNo", row.get("kb_no"));
        detail.put("title", row.get("name"));
        detail.put("kbType", row.get("kb_type"));
        detail.put("summary", row.get("summary"));
        detail.put("intro", row.get("intro"));
        detail.put("displayType", row.get("display_type"));
        detail.put("coverUrl", row.get("cover_url"));
        detail.put("authorName", row.get("author_name"));
        detail.put("publishedAt", row.get("published_at"));
        detail.put("subscribeCount", row.get("subscribe_count"));
        detail.put("chapters", buildTree(id));
        detail.put("subscribed", false);
        Long userId = optionalUserId(auth);
        if (userId != null) {
            detail.put("subscribed", subscriptionMapper.countByUserAndBase(userId, id) > 0);
        }
        return Result.success(detail);
    }

    /** 文章详情（阅读页正文） */
    @GetMapping("/article/{articleId}")
    public Result<Map<String, Object>> article(@PathVariable Long articleId) {
        Map<String, Object> row = knowledgeMapper.selectArticleDetail(articleId);
        if (row == null) {
            throw new BusinessException(404, "内容不存在或未发布");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", row.get("id"));
        detail.put("baseId", row.get("base_id"));
        detail.put("moduleId", row.get("module_id"));
        detail.put("title", row.get("title"));
        detail.put("summary", row.get("summary"));
        detail.put("content", row.get("content"));
        detail.put("authorName", row.get("author_name"));
        detail.put("publishedAt", row.get("published_at"));
        return Result.success(detail);
    }

    /** 阅读目录：指定知识库的模块-文章树（阅读页左侧目录） */
    @GetMapping("/{baseId}/chapters")
    public Result<List<Map<String, Object>>> chapters(@PathVariable Long baseId) {
        return Result.success(buildTree(baseId));
    }

    /** 推荐列表（同分类优先） */
    @GetMapping("/recommend")
    public Result<List<KnowledgeCardVO>> recommend(@RequestParam(required = false) Long excludeId,
                                                   @RequestParam(required = false) String kbType,
                                                   @RequestParam(defaultValue = "8") int limit) {
        return Result.success(baseMapper.selectRecommend(excludeId, kbType, limit));
    }

    /** 订阅知识库（需登录） */
    @PostMapping("/{baseId}/subscribe")
    public Result<Void> subscribe(@PathVariable Long baseId,
                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = requireUserId(auth);
        if (baseMapper.selectById(baseId) == null) {
            throw new BusinessException(404, "内容不存在");
        }
        if (subscriptionMapper.countByUserAndBase(userId, baseId) > 0) {
            return Result.success();
        }
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setBaseId(baseId);
        subscriptionMapper.insert(sub);
        baseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, baseId)
                .setSql("subscribe_count = subscribe_count + 1"));
        return Result.success();
    }

    /** 取消订阅（需登录） */
    @DeleteMapping("/{baseId}/subscribe")
    public Result<Void> unsubscribe(@PathVariable Long baseId,
                                    @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = requireUserId(auth);
        subscriptionMapper.delete(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getBaseId, baseId));
        baseMapper.update(null, new LambdaUpdateWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, baseId)
                .setSql("subscribe_count = GREATEST(subscribe_count - 1, 0)"));
        return Result.success();
    }

    /** 我的订阅分页（需登录） */
    @GetMapping("/my/subscriptions")
    public Result<PageVO<KnowledgeCardVO>> mySubscriptions(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "8") long size) {
        Long userId = requireUserId(auth);
        long total = baseMapper.countMySubscriptions(userId);
        List<KnowledgeCardVO> list = total == 0 ? List.of()
                : baseMapper.selectMySubscriptions(userId, (page - 1) * size, (int) size);
        return Result.success(new PageVO<>(total, list));
    }

    /** 模块-文章树：[{moduleId, moduleName, articles:[{id,title}]}] */
    private List<Map<String, Object>> buildTree(Long baseId) {
        List<KnowledgeModule> modules = moduleMapper.selectList(new LambdaQueryWrapper<KnowledgeModule>()
                .eq(KnowledgeModule::getBaseId, baseId)
                .orderByAsc(KnowledgeModule::getSortOrder)
                .orderByAsc(KnowledgeModule::getId));
        List<Knowledge> articles = knowledgeMapper.selectList(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getBaseId, baseId)
                .orderByAsc(Knowledge::getId));

        Map<Long, Map<String, Object>> moduleMap = new LinkedHashMap<>();
        for (KnowledgeModule m : modules) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("moduleId", m.getId());
            node.put("moduleName", m.getModuleName());
            node.put("articles", new ArrayList<Map<String, Object>>());
            moduleMap.put(m.getId(), node);
        }
        for (Knowledge k : articles) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", k.getId());
            a.put("title", k.getTitle());
            a.put("moduleId", k.getModuleId());
            Map<String, Object> node = moduleMap.get(k.getModuleId());
            if (node != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> arr = (List<Map<String, Object>>) node.get("articles");
                arr.add(a);
            }
        }
        return new ArrayList<>(moduleMap.values());
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

    private Long optionalUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtUtil.parseUserId(auth.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
