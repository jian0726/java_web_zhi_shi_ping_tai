package com.haoyou.service.notify.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.notify.entity.NotifyMessage;
import com.haoyou.service.notify.mapper.NotifyMessageMapper;
import com.haoyou.service.notify.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内通知（走网关 /api/notify/** 路由，直连 59857 亦可）
 */
@RestController
@RequestMapping("/api/notify")
public class NotifyController {

    private final NotifyMessageMapper notifyMapper;
    private final JwtUtil jwtUtil;

    public NotifyController(NotifyMessageMapper notifyMapper, JwtUtil jwtUtil) {
        this.notifyMapper = notifyMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 我的通知分页 */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestHeader("Authorization") String auth,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        Long userId = requireUserId(auth);
        long total = notifyMapper.selectCount(new LambdaQueryWrapper<NotifyMessage>()
                .eq(NotifyMessage::getUserId, userId));
        List<NotifyMessage> list = total == 0 ? List.of()
                : notifyMapper.selectList(new LambdaQueryWrapper<NotifyMessage>()
                        .eq(NotifyMessage::getUserId, userId)
                        .orderByDesc(NotifyMessage::getCreatedAt)
                        .last("LIMIT " + ((page - 1) * size) + "," + size));
        long unread = notifyMapper.selectCount(new LambdaQueryWrapper<NotifyMessage>()
                .eq(NotifyMessage::getUserId, userId)
                .eq(NotifyMessage::getIsRead, 0));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("unread", unread);
        data.put("list", list);
        return Result.success(data);
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> read(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = requireUserId(auth);
        notifyMapper.update(null, new LambdaUpdateWrapper<NotifyMessage>()
                .eq(NotifyMessage::getId, id)
                .eq(NotifyMessage::getUserId, userId)
                .set(NotifyMessage::getIsRead, 1));
        return Result.success();
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
