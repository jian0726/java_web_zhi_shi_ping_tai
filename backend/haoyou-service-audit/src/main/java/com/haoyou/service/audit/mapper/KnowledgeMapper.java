package com.haoyou.service.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.audit.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    /** 审核列表分页（可按状态过滤，含作者昵称） */
    @Select("<script>" +
            "SELECT k.id, k.article_no AS articleNo, k.title, k.kb_type AS kbType, k.summary, " +
            "       k.status, k.current_version AS currentVersion, k.published_at AS publishedAt, " +
            "       k.created_at AS createdAt, k.updated_at AS updatedAt, " +
            "       IFNULL(u.nickname, '佚名') AS authorName " +
            "FROM knowledge k LEFT JOIN sys_user u ON u.id = k.author_id " +
            "WHERE k.deleted = 0 " +
            "<if test='status != null'>AND k.status = #{status}</if>" +
            " ORDER BY k.updated_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectAuditPage(@Param("status") Integer status,
                                              @Param("offset") long offset,
                                              @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM knowledge k WHERE k.deleted = 0 " +
            "<if test='status != null'>AND k.status = #{status}</if>" +
            "</script>")
    long countAuditPage(@Param("status") Integer status);

    /** 审核详情（含正文与作者） */
    @Select("SELECT k.*, IFNULL(u.nickname, '佚名') AS author_name " +
            "FROM knowledge k LEFT JOIN sys_user u ON u.id = k.author_id " +
            "WHERE k.id = #{id} AND k.deleted = 0")
    Map<String, Object> selectDetail(@Param("id") Long id);
}
