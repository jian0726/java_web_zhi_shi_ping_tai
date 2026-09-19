package com.haoyou.service.user.controller;

import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.user.dto.ProfileResponse;
import com.haoyou.service.user.entity.SysUser;
import com.haoyou.service.user.mapper.SysUserMapper;
import com.haoyou.service.user.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料接口（走网关 /api/user/** 路由，直连 8081 亦可）
 */
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;

    public ProfileController(SysUserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 当前登录用户资料（需登录） */
    @GetMapping("/profile")
    public Result<ProfileResponse> profile(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        Long userId;
        try {
            userId = Long.valueOf(jwtUtil.parse(auth.substring(7)).getSubject());
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        ProfileResponse resp = new ProfileResponse();
        resp.setId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setOccupation(user.getOccupation());
        resp.setWorkYears(user.getWorkYears());
        resp.setWorkStatus(user.getWorkStatus());
        resp.setIdCard(user.getIdCard());
        resp.setWorkProof(user.getWorkProof());
        return Result.success(resp);
    }

    /** 修改个人资料（昵称；需登录） */
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody java.util.Map<String, Object> body) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        Long userId;
        try {
            userId = Long.valueOf(jwtUtil.parse(auth.substring(7)).getSubject());
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        Object nickname = body.get("nickname");
        if (nickname != null && !String.valueOf(nickname).isBlank()) {
            user.setNickname(String.valueOf(nickname).trim());
        }
        Object avatar = body.get("avatar");
        if (avatar != null && !String.valueOf(avatar).isBlank()) {
            user.setAvatar(String.valueOf(avatar));
        }
        // 创作者认证字段（全量可更新，传空串视为清除）
        user.setOccupation(strOrNull(body.get("occupation"), user.getOccupation()));
        user.setWorkYears(strOrNull(body.get("workYears"), user.getWorkYears()));
        user.setWorkStatus(strOrNull(body.get("workStatus"), user.getWorkStatus()));
        user.setIdCard(strOrNull(body.get("idCard"), user.getIdCard()));
        user.setWorkProof(strOrNull(body.get("workProof"), user.getWorkProof()));
        userMapper.updateById(user);
        return Result.success();
    }

    /** 传 null 忽略；传值（含空串）则更新 */
    private String strOrNull(Object raw, String fallback) {
        return raw == null ? fallback : String.valueOf(raw);
    }
}
