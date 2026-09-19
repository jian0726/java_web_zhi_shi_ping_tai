package com.haoyou.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 用户-角色关联 sys_user_role（联合主键，无自增 id）
 */
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private Long userId;
    private Integer roleId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}
