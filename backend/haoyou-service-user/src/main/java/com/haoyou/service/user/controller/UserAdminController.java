package com.haoyou.service.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.user.entity.SysRole;
import com.haoyou.service.user.entity.SysUser;
import com.haoyou.service.user.entity.SysUserRole;
import com.haoyou.service.user.mapper.SysRoleMapper;
import com.haoyou.service.user.mapper.SysUserMapper;
import com.haoyou.service.user.mapper.SysUserRoleMapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端：用户管理（走网关 /api/user/** 路由，直连 59851 亦可）
 */
@RestController
@RequestMapping("/api/user/admin")
public class UserAdminController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public UserAdminController(SysUserMapper userMapper,
                               SysRoleMapper roleMapper,
                               SysUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    /** 用户分页：keyword 模糊匹配手机号/昵称 */
    @GetMapping("/users")
    public Result<Map<String, Object>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getPhone, keyword).or().like(SysUser::getNickname, keyword));
        }
        long total = userMapper.selectCount(wrapper);
        wrapper.orderByDesc(SysUser::getCreatedAt).last("LIMIT " + ((page - 1) * size) + "," + size);
        List<SysUser> users = userMapper.selectList(wrapper);

        Map<Integer, String> roleNameMap = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
        List<Map<String, Object>> list = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("phone", u.getPhone());
            m.put("nickname", u.getNickname());
            m.put("status", u.getStatus());
            m.put("createdAt", u.getCreatedAt());
            List<Integer> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, u.getId()))
                    .stream().map(SysUserRole::getRoleId).toList();
            m.put("roleIds", roleIds);
            m.put("roleNames", roleIds.stream().map(r -> roleNameMap.getOrDefault(r, "?")).toList());
            return m;
        }).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("list", list);
        return Result.success(data);
    }

    /** 启用/禁用：status 1 启用 0 禁用 */
    @PutMapping("/user/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status;
        try {
            status = Integer.valueOf(String.valueOf(body.get("status")));
        } catch (Exception e) {
            throw new BusinessException(400, "status 必须为 0 或 1");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "status 必须为 0 或 1");
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    /** 角色列表 */
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(roleMapper.selectList(null));
    }

    /** 分配角色（全量覆盖） */
    @PutMapping("/user/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在");
        }
        Object raw = body.get("roleIds");
        if (!(raw instanceof List<?> roleIds)) {
            throw new BusinessException(400, "roleIds 必须为数组");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        for (Object r : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(id);
            ur.setRoleId(Integer.valueOf(String.valueOf(r)));
            userRoleMapper.insert(ur);
        }
        return Result.success();
    }
}
