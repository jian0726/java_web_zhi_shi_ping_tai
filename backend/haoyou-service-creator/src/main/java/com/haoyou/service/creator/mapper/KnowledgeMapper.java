package com.haoyou.service.creator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.creator.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    /** 文章详情（含作者昵称，用于回填编辑） */
    @Select("SELECT k.*, IFNULL(u.nickname, '佚名') AS author_name " +
            "FROM knowledge k LEFT JOIN sys_user u ON u.id = k.author_id " +
            "WHERE k.id = #{id} AND k.deleted = 0")
    Map<String, Object> selectArticleDetail(@Param("id") Long id);
}
