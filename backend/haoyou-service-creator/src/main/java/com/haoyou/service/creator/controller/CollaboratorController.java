package com.haoyou.service.creator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.creator.entity.KnowledgeBase;
import com.haoyou.service.creator.entity.KnowledgeCollaborator;
import com.haoyou.service.creator.mapper.KnowledgeBaseMapper;
import com.haoyou.service.creator.mapper.KnowledgeCollaboratorMapper;
import com.haoyou.service.creator.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库协作者管理（仅知识库所有者可操作；collaborator.knowledge_id 列语义为 base_id）
 */
@RestController
@RequestMapping("/api/creator/base/{baseId}/collaborators")
public class CollaboratorController {

    private final KnowledgeCollaboratorMapper collaboratorMapper;
    private final KnowledgeBaseMapper baseMapper;
    private final JwtUtil jwtUtil;

    public CollaboratorController(KnowledgeCollaboratorMapper collaboratorMapper,
                                  KnowledgeBaseMapper baseMapper,
                                  JwtUtil jwtUtil) {
        this.collaboratorMapper = collaboratorMapper;
        this.baseMapper = baseMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 协作者列表 */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestHeader("Authorization") String auth,
                                                  @PathVariable Long baseId) {
        requireOwner(auth, baseId);
        return Result.success(collaboratorMapper.selectWithUser(baseId));
    }

    /** 添加协作者：按手机号查找用户 */
    @PostMapping
    public Result<Long> add(@RequestHeader("Authorization") String auth,
                            @PathVariable Long baseId,
                            @RequestBody Map<String, String> body) {
        Long userId = requireOwner(auth, baseId);
        String phone = body.get("phone") == null ? "" : body.get("phone").trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        Map<String, Object> user = collaboratorMapper.findUserByPhone(phone);
        if (user == null) {
            throw new BusinessException(404, "该手机号未注册");
        }
        Long targetId = Long.valueOf(String.valueOf(user.get("id")));
        if (targetId.equals(userId)) {
            throw new BusinessException(400, "不能添加自己");
        }
        Long exists = collaboratorMapper.selectCount(new LambdaQueryWrapper<KnowledgeCollaborator>()
                .eq(KnowledgeCollaborator::getKnowledgeId, baseId)
                .eq(KnowledgeCollaborator::getUserId, targetId));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "该用户已是协作者");
        }
        KnowledgeCollaborator c = new KnowledgeCollaborator();
        c.setKnowledgeId(baseId);
        c.setUserId(targetId);
        c.setRole("MEMBER");
        collaboratorMapper.insert(c);
        return Result.success(c.getId());
    }

    /** 移除协作者（所有者操作；OWNER 不可移除） */
    @DeleteMapping("/{collaboratorId}")
    public Result<Void> remove(@RequestHeader("Authorization") String auth,
                               @PathVariable Long baseId,
                               @PathVariable Long collaboratorId) {
        requireOwner(auth, baseId);
        KnowledgeCollaborator c = collaboratorMapper.selectById(collaboratorId);
        if (c == null || !c.getKnowledgeId().equals(baseId)) {
            throw new BusinessException(404, "协作者不存在");
        }
        if ("OWNER".equals(c.getRole())) {
            throw new BusinessException(400, "不能移除所有者");
        }
        collaboratorMapper.deleteById(collaboratorId);
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
            throw new BusinessException(403, "仅知识库所有者可管理协作者");
        }
        return userId;
    }
}
