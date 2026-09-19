package com.haoyou.service.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoyou.common.BusinessException;
import com.haoyou.service.user.dto.LoginRequest;
import com.haoyou.service.user.dto.LoginResponse;
import com.haoyou.service.user.dto.RegisterRequest;
import com.haoyou.service.user.entity.SysUser;
import com.haoyou.service.user.mapper.SysUserMapper;
import com.haoyou.service.user.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;

/**
 * 注册 / 登录（FR-01~06 最小实现）
 */
@Service
public class AuthService {

    private static final String SMS_KEY_PREFIX = "sms:code:";

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public AuthService(SysUserMapper userMapper, JwtUtil jwtUtil, StringRedisTemplate redis) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.redis = redis;
    }

    public void register(RegisterRequest req) {
        if (req.getPhone() == null || !req.getPhone().matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new BusinessException(400, "密码至少 6 位");
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, req.getPhone()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "该手机号已注册");
        }
        SysUser user = new SysUser();
        user.setPhone(req.getPhone());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? "用户" + req.getPhone().substring(7) : req.getNickname());
        user.setStatus(1);
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        if (req.getPhone() == null || req.getPassword() == null) {
            throw new BusinessException(400, "手机号和密码不能为空");
        }
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, req.getPhone()));
        if (user == null || !encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.sign(user.getId(), user.getNickname());
        return new LoginResponse(token, user.getNickname(), user.getPhone());
    }

    /**
     * 发送短信验证码。未接短信服务商：验证码存 Redis 5 分钟，
     * 开发模式直接回显（接入阿里云/腾讯云短信后改为真实下发并去掉 code 字段）。
     */
    public Map<String, String> sendSmsCode(String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        String code = String.format("%06d", random.nextInt(1000000));
        redis.opsForValue().set(SMS_KEY_PREFIX + phone, code, Duration.ofMinutes(5));
        return Map.of(
                "code", code,
                "tip", "开发环境未接入短信服务商，验证码直接回显，5 分钟内有效"
        );
    }

    /** 验证码登录（未注册手机号自动注册） */
    public LoginResponse loginBySms(String phone, String code) {
        if (phone == null || code == null) {
            throw new BusinessException(400, "手机号和验证码不能为空");
        }
        String cached = redis.opsForValue().get(SMS_KEY_PREFIX + phone);
        if (cached == null || !cached.equals(code.trim())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        redis.delete(SMS_KEY_PREFIX + phone);
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            // 自动注册
            user = new SysUser();
            user.setPhone(phone);
            user.setPasswordHash(encoder.encode("sms-" + phone));
            user.setNickname("用户" + phone.substring(7));
            user.setStatus(1);
            userMapper.insert(user);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.sign(user.getId(), user.getNickname());
        return new LoginResponse(token, user.getNickname(), user.getPhone());
    }
}
