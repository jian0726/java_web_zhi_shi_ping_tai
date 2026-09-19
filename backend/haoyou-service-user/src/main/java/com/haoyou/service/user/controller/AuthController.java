package com.haoyou.service.user.controller;

import com.haoyou.common.Result;
import com.haoyou.service.user.dto.LoginRequest;
import com.haoyou.service.user.dto.LoginResponse;
import com.haoyou.service.user.dto.RegisterRequest;
import com.haoyou.service.user.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（走网关 /api/user/** 路由，直连 8081 亦可）
 */
@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    /** 发送短信验证码（开发模式回显） */
    @PostMapping("/sms/send")
    public Result<java.util.Map<String, String>> sendSms(@RequestBody java.util.Map<String, String> body) {
        return Result.success(authService.sendSmsCode(body.get("phone")));
    }

    /** 验证码登录（未注册自动注册） */
    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@RequestBody java.util.Map<String, String> body) {
        return Result.success(authService.loginBySms(body.get("phone"), body.get("code")));
    }
}
