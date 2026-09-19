package com.haoyou.service.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.kb.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    /** 文章详情（限已发布知识库下） */
    @Select("SELECT k.*, IFNULL(u.nickname, '佚名') AS author_name " +
            "FROM knowledge k " +
            "JOIN knowledge_base b ON b.id = k.base_id AND b.status = 2 AND b.deleted = 0 " +
            "LEFT JOIN sys_user u ON u.id = k.author_id " +
            "WHERE k.id = #{id} AND k.deleted = 0")
    Map<String, Object> selectArticleDetail(@Param("id") Long id);
}
