package com.haoyou.service.creator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.creator.dto.AuditApplyRequest;
import com.haoyou.service.creator.dto.PageVO;
import com.haoyou.service.creator.entity.CreatorApply;
import com.haoyou.service.creator.entity.SysRole;
import com.haoyou.service.creator.entity.SysUserRole;
import com.haoyou.service.creator.mapper.CreatorApplyMapper;
import com.haoyou.service.creator.mapper.SysRoleMapper;
import com.haoyou.service.creator.mapper.SysUserRoleMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理端：创作者申请审核 + 创作者信息列表
 */
@RestController
@RequestMapping("/api/creator/admin")
public class CreatorAdminController {

    /** 审核通过后自动授予的角色：创作者 */
    private static final String CREATOR_CODE = "CREATOR";

    private final CreatorApplyMapper applyMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public CreatorAdminController(CreatorApplyMapper applyMapper,
                                  SysRoleMapper roleMapper,
                                  SysUserRoleMapper userRoleMapper) {
        this.applyMapper = applyMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    /** 申请分页：status 可选（0待审 1通过 2驳回） */
    @GetMapping("/applies")
    public Result<PageVO<Map<String, Object>>> applies(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        long total = applyMapper.countApplyPage(status);
        List<Map<String, Object>> list = total == 0 ? List.of()
                : applyMapper.selectApplyPage(status, (page - 1) * size, (int) size);
        return Result.success(new PageVO<>(total, list));
    }

    /** 审核创作者申请：action 1 通过 2 驳回 */
    @PutMapping("/apply/{id}/audit")
    public Result<Void> auditApply(@PathVariable Long id, @RequestBody AuditApplyRequest req) {
        if (req.getAction() == null || (req.getAction() != 1 && req.getAction() != 2)) {
            throw new BusinessException(400, "action 必须为 1(通过) 或 2(驳回)");
        }
        if (req.getAction() == 2 && (req.getRemark() == null || req.getRemark().isBlank())) {
            throw new BusinessException(400, "驳回必须填写理由");
        }
        CreatorApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(404, "申请不存在");
        }
        if (apply.getStatus() != null && apply.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理");
        }
        apply.setStatus(req.getAction() == 1 ? 1 : 2);
        apply.setAuditRemark(req.getRemark());
        apply.setAuditedAt(LocalDateTime.now());
        applyMapper.updateById(apply);
        // 审核通过：自动授予申请人"创作者"角色（幂等）
        if (req.getAction() == 1) {
            grantCreatorRole(apply.getUserId());
        }
        return Result.success();
    }

    /** 授予创作者角色（已存在则跳过） */
    private void grantCreatorRole(Long userId) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, CREATOR_CODE));
        if (role == null) {
            throw new BusinessException(500, "角色字典缺少 CREATOR，请检查 sys_role 表");
        }
        Long exists = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId).eq(SysUserRole::getRoleId, role.getId()));
        if (exists != null && exists > 0) {
            return;
        }
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);
    }

    /** 创作者信息列表（申请通过者） */
    @GetMapping("/creators")
    public Result<PageVO<Map<String, Object>>> creators(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        long total = applyMapper.countCreators();
        List<Map<String, Object>> list = total == 0 ? List.of()
                : applyMapper.selectCreators((page - 1) * size, (int) size);
        return Result.success(new PageVO<>(total, list));
    }
}
