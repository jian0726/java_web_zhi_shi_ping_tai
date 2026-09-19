package com.haoyou.service.creator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.creator.entity.CreatorApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CreatorApplyMapper extends BaseMapper<CreatorApply> {

    /** 管理端申请列表（含申请人昵称/手机号） */
    @Select("<script>" +
            "SELECT a.id, a.user_id AS userId, a.reason, a.qualification, a.status, " +
            "       a.audit_remark AS auditRemark, a.audited_at AS auditedAt, a.created_at AS createdAt, " +
            "       u.nickname, u.phone " +
            "FROM creator_apply a LEFT JOIN sys_user u ON u.id = a.user_id " +
            "WHERE a.deleted = 0 " +
            "<if test='status != null'>AND a.status = #{status}</if>" +
            " ORDER BY a.created_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectApplyPage(@Param("status") Integer status,
                                              @Param("offset") long offset,
                                              @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM creator_apply a WHERE a.deleted = 0 " +
            "<if test='status != null'>AND a.status = #{status}</if>" +
            "</script>")
    long countApplyPage(@Param("status") Integer status);

    /** 创作者信息列表（申请通过者） */
    @Select("SELECT a.id AS applyId, a.user_id AS userId, u.nickname, u.phone, " +
            "       a.reason, a.qualification, a.audited_at AS auditedAt " +
            "FROM creator_apply a LEFT JOIN sys_user u ON u.id = a.user_id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "ORDER BY a.audited_at DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectCreators(@Param("offset") long offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM creator_apply WHERE deleted = 0 AND status = 1")
    long countCreators();
}
