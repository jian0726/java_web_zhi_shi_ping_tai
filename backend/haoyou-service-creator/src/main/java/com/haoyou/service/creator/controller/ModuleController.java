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

import java.util.List;
import java.util.Map;

/**
 * 创作者端：知识模块管理（知识库下的章节，仅所有者可操作）
 */
@RestController
@RequestMapping("/api/creator/base/{baseId}/module")
public class ModuleController {

    private final KnowledgeModuleMapper moduleMapper;
    private final KnowledgeBaseMapper baseMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final JwtUtil jwtUtil;

    public ModuleController(KnowledgeModuleMapper moduleMapper,
                            KnowledgeBaseMapper baseMapper,
                            KnowledgeMapper knowledgeMapper,
                            JwtUtil jwtUtil) {
        this.moduleMapper = moduleMapper;
        this.baseMapper = baseMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 模块列表（按 sort_order 排序） */
    @GetMapping("/list")
    public Result<List<KnowledgeModule>> list(@RequestHeader("Authorization") String auth,
                                              @PathVariable Long baseId) {
        requireOwner(auth, baseId);
        return Result.success(moduleMapper.selectList(new LambdaQueryWrapper<KnowledgeModule>()
                .eq(KnowledgeModule::getBaseId, baseId)
                .orderByAsc(KnowledgeModule::getSortOrder)
                .orderByAsc(KnowledgeModule::getId)));
    }

    /** 创建模块 */
    @PostMapping
    public Result<Long> create(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @RequestBody Map<String, Object> body) {
        requireOwner(auth, baseId);
        String name = body.get("moduleName") == null ? "" : String.valueOf(body.get("moduleName")).trim();
        if (name.isEmpty()) {
            throw new BusinessException(400, "模块名称不能为空");
        }
        Long count = moduleMapper.selectCount(new LambdaQueryWrapper<KnowledgeModule>()
                .eq(KnowledgeModule::getBaseId, baseId));
        KnowledgeModule m = new KnowledgeModule();
        m.setBaseId(baseId);
        m.setModuleName(name);
        m.setSortOrder(count == null ? 0 : count.intValue());
        moduleMapper.insert(m);
        return Result.success(m.getId());
    }

    /** 修改模块 */
    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        requireOwner(auth, baseId);
        KnowledgeModule m = moduleMapper.selectById(id);
        if (m == null || !m.getBaseId().equals(baseId)) {
            throw new BusinessException(404, "模块不存在");
        }
        if (body.get("moduleName") != null) {
            m.setModuleName(String.valueOf(body.get("moduleName")).trim());
        }
        if (body.get("sortOrder") != null) {
            m.setSortOrder(Integer.valueOf(String.valueOf(body.get("sortOrder"))));
        }
        moduleMapper.updateById(m);
        return Result.success();
    }

    /** 删除模块（模块下有文章则拒绝） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @PathVariable Long id) {
        requireOwner(auth, baseId);
        KnowledgeModule m = moduleMapper.selectById(id);
        if (m == null || !m.getBaseId().equals(baseId)) {
            throw new BusinessException(404, "模块不存在");
        }
        Long articleCount = knowledgeMapper.selectCount(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getModuleId, id));
        if (articleCount != null && articleCount > 0) {
            throw new BusinessException(400, "模块下还有 " + articleCount + " 篇文章，请先删除文章");
        }
        moduleMapper.deleteById(id);
        return Result.success();
    }

    private KnowledgeBase requireOwner(String auth, Long baseId) {
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
        return base;
    }
}
