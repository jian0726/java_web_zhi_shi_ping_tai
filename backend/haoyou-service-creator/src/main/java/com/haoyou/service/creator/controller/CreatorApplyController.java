package com.haoyou.service.creator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.creator.entity.CreatorApply;
import com.haoyou.service.creator.mapper.CreatorApplyMapper;
import com.haoyou.service.creator.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 创作者申请（用户侧）
 */
@RestController
@RequestMapping("/api/creator/apply")
public class CreatorApplyController {

    private final CreatorApplyMapper applyMapper;
    private final JwtUtil jwtUtil;

    public CreatorApplyController(CreatorApplyMapper applyMapper, JwtUtil jwtUtil) {
        this.applyMapper = applyMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 提交创作者申请（存在待审/已通过申请时不允许重复提交） */
    @PostMapping
    public Result<Long> apply(@RequestHeader("Authorization") String auth,
                              @RequestBody com.haoyou.service.creator.dto.ApplyRequest req) {
        Long userId = requireUserId(auth);
        Long pending = applyMapper.selectCount(new LambdaQueryWrapper<CreatorApply>()
                .eq(CreatorApply::getUserId, userId)
                .in(CreatorApply::getStatus, 0, 1));
        if (pending != null && pending > 0) {
            throw new BusinessException(400, "已有待审核或已通过的申请");
        }
        CreatorApply apply = new CreatorApply();
        apply.setUserId(userId);
        apply.setReason(req.getReason());
        apply.setQualification(req.getQualification());
        apply.setStatus(0);
        applyMapper.insert(apply);
        return Result.success(apply.getId());
    }

    /** 我的最新申请状态 */
    @GetMapping("/my")
    public Result<Map<String, Object>> my(@RequestHeader("Authorization") String auth) {
        Long userId = requireUserId(auth);
        CreatorApply apply = applyMapper.selectOne(new LambdaQueryWrapper<CreatorApply>()
                .eq(CreatorApply::getUserId, userId)
                .orderByDesc(CreatorApply::getCreatedAt)
                .last("LIMIT 1"));
        if (apply == null) {
            return Result.success(null);
        }
        return Result.success(Map.of(
                "id", apply.getId(),
                "status", apply.getStatus(),
                "auditRemark", apply.getAuditRemark() == null ? "" : apply.getAuditRemark(),
                "createdAt", String.valueOf(apply.getCreatedAt())
        ));
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
