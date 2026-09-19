package com.haoyou.service.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.audit.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /** 审核列表分页（可按状态过滤，含作者昵称与模块/文章计数） */
    @Select("<script>" +
            "SELECT b.id, b.kb_no AS kbNo, b.name, b.kb_type AS kbType, b.summary, " +
            "       b.status, b.current_version AS currentVersion, b.published_at AS publishedAt, " +
            "       b.created_at AS createdAt, b.updated_at AS updatedAt, " +
            "       IFNULL(u.nickname, '佚名') AS authorName, " +
            "       (SELECT COUNT(*) FROM knowledge_module m WHERE m.base_id = b.id AND m.deleted = 0) AS moduleCount, " +
            "       (SELECT COUNT(*) FROM knowledge k WHERE k.base_id = b.id AND k.deleted = 0) AS articleCount " +
            "FROM knowledge_base b LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE b.deleted = 0 " +
            "<if test='status != null'>AND b.status = #{status}</if>" +
            " ORDER BY b.updated_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectAuditPage(@Param("status") Integer status,
                                              @Param("offset") long offset,
                                              @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM knowledge_base b WHERE b.deleted = 0 " +
            "<if test='status != null'>AND b.status = #{status}</if>" +
            "</script>")
    long countAuditPage(@Param("status") Integer status);

    /** 审核详情（含介绍与作者） */
    @Select("SELECT b.*, IFNULL(u.nickname, '佚名') AS author_name " +
            "FROM knowledge_base b LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE b.id = #{id} AND b.deleted = 0")
    Map<String, Object> selectDetail(@Param("id") Long id);
}
