package com.haoyou.service.user.dto;

/**
 * 登录响应
 */
public class LoginResponse {

    private String token;
    private String nickname;
    private String phone;

    public LoginResponse() {
    }

    public LoginResponse(String token, String nickname, String phone) {
        this.token = token;
        this.nickname = nickname;
        this.phone = phone;
    }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }
}
