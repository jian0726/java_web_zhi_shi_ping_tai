package com.haoyou.service.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.kb.dto.KnowledgeCardVO;
import com.haoyou.service.kb.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /** 分类列表（仅已发布知识库），带数量统计 */
    @Select("SELECT kb_type AS type, COUNT(*) AS count FROM knowledge_base " +
            "WHERE status = 2 AND deleted = 0 AND kb_type IS NOT NULL " +
            "GROUP BY kb_type ORDER BY count DESC")
    List<Map<String, Object>> selectCategories();

    /** 分页查询已发布知识库（含作者昵称），type 为空查全部 */
    @Select("<script>" +
            "SELECT b.id, b.name AS title, b.kb_type AS kbType, b.summary, b.cover_url AS coverUrl, " +
            "       b.published_at AS publishedAt, b.subscribe_count AS subscribeCount, " +
            "       IFNULL(u.nickname, '佚名') AS authorName, " +
            "       (SELECT COUNT(*) FROM knowledge k WHERE k.base_id = b.id AND k.deleted = 0) AS articleCount " +
            "FROM knowledge_base b LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE b.status = 2 AND b.deleted = 0 " +
            "<if test='type != null and type != \"\"'>AND b.kb_type = #{type}</if>" +
            "<choose>" +
            "  <when test=\"sort == 'hot'\">ORDER BY b.subscribe_count DESC, b.id DESC</when>" +
            "  <otherwise>ORDER BY b.published_at DESC, b.id DESC</otherwise>" +
            "</choose>" +
            " LIMIT #{offset}, #{size}" +
            "</script>")
    List<KnowledgeCardVO> selectPage(@Param("type") String type, @Param("sort") String sort,
                                     @Param("offset") long offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM knowledge_base b WHERE b.status = 2 AND b.deleted = 0 " +
            "<if test='type != null and type != \"\"'>AND b.kb_type = #{type}</if>" +
            "</script>")
    long countPage(@Param("type") String type);

    /** 知识库详情（含作者昵称），仅已发布 */
    @Select("SELECT b.*, IFNULL(u.nickname, '佚名') AS author_name " +
            "FROM knowledge_base b LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE b.id = #{id} AND b.status = 2 AND b.deleted = 0")
    Map<String, Object> selectDetail(@Param("id") Long id);

    /** 推荐列表（同分类优先，不足补最新） */
    @Select("SELECT b.id, b.name AS title, b.summary, b.cover_url AS coverUrl, " +
            "       b.published_at AS publishedAt, b.subscribe_count AS subscribeCount, " +
            "       IFNULL(u.nickname, '佚名') AS authorName, " +
            "       (SELECT COUNT(*) FROM knowledge k WHERE k.base_id = b.id AND k.deleted = 0) AS articleCount " +
            "FROM knowledge_base b LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE b.status = 2 AND b.deleted = 0 AND b.id != #{excludeId} " +
            "ORDER BY (b.kb_type = #{kbType}) DESC, b.subscribe_count DESC, b.id DESC " +
            "LIMIT #{limit}")
    List<KnowledgeCardVO> selectRecommend(@Param("excludeId") Long excludeId,
                                          @Param("kbType") String kbType, @Param("limit") int limit);

    /** 我的订阅分页（订阅对象为知识库） */
    @Select("SELECT b.id, b.name AS title, b.summary, b.cover_url AS coverUrl, " +
            "       b.published_at AS publishedAt, b.subscribe_count AS subscribeCount, " +
            "       IFNULL(u.nickname, '佚名') AS authorName, " +
            "       (SELECT COUNT(*) FROM knowledge k WHERE k.base_id = b.id AND k.deleted = 0) AS articleCount " +
            "FROM subscription s JOIN knowledge_base b ON b.id = s.base_id " +
            "LEFT JOIN sys_user u ON u.id = b.author_id " +
            "WHERE s.user_id = #{userId} AND b.deleted = 0 " +
            "ORDER BY s.created_at DESC LIMIT #{offset}, #{size}")
    List<KnowledgeCardVO> selectMySubscriptions(@Param("userId") Long userId,
                                                @Param("offset") long offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM subscription s JOIN knowledge_base b ON b.id = s.base_id " +
            "WHERE s.user_id = #{userId} AND b.deleted = 0")
    long countMySubscriptions(@Param("userId") Long userId);
}
