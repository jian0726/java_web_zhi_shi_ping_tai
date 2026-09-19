package com.haoyou.service.creator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.creator.entity.KnowledgeCollaborator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeCollaboratorMapper extends BaseMapper<KnowledgeCollaborator> {

    /** 协作者列表（含用户昵称/手机号） */
    @Select("SELECT c.id, c.user_id AS userId, c.role, c.created_at AS createdAt, " +
            "       IFNULL(u.nickname, '佚名') AS nickname, u.phone " +
            "FROM knowledge_collaborator c LEFT JOIN sys_user u ON u.id = c.user_id " +
            "WHERE c.knowledge_id = #{knowledgeId} ORDER BY c.id ASC")
    List<Map<String, Object>> selectWithUser(@Param("knowledgeId") Long knowledgeId);

    /** 按手机号查用户（添加协作者用） */
    @Select("SELECT id, IFNULL(nickname, '佚名') AS nickname, phone FROM sys_user " +
            "WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    Map<String, Object> findUserByPhone(@Param("phone") String phone);
}
