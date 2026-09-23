package com.haoyou.service.user.dto;

import java.util.List;

/**
 * 登录响应
 */
public class LoginResponse {

    private String token;
    private String nickname;
    private String phone;
    /** 角色编码列表（READER/CREATOR/AUDITOR/ADMIN） */
    private List<String> roleCodes;

    public LoginResponse() {
    }

    public LoginResponse(String token, String nickname, String phone) {
        this(token, nickname, phone, List.of());
    }

    public LoginResponse(String token, String nickname, String phone, List<String> roleCodes) {
        this.token = token;
        this.nickname = nickname;
        this.phone = phone;
        this.roleCodes = roleCodes;
    }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getRoleCodes() { return roleCodes; }

    public void setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; }
}
