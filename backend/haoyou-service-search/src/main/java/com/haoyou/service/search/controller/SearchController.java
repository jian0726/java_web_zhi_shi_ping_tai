package com.haoyou.service.search.controller;

import com.haoyou.common.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 检索服务（走网关 /api/search/** 路由，直连 59856 亦可）
 * 最小实现：MySQL LIKE 查询已发布文章；ES 全文检索待环境就绪后切换。
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchMapper searchMapper;

    public SearchController(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
    }

    /** 关键词检索已发布文章（标题/摘要/正文） */
    @GetMapping
    public Result<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            throw new com.haoyou.common.BusinessException(400, "关键词不能为空");
        }
        String like = "%" + kw + "%";
        long total = searchMapper.countSearch(like, type);
        List<Map<String, Object>> list = total == 0 ? List.of()
                : searchMapper.search(like, type, (page - 1) * size, (int) size);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("keyword", kw);
        data.put("list", list);
        return Result.success(data);
    }

    @Mapper
    public interface SearchMapper {
        @Select("<script>" +
                "SELECT COUNT(*) FROM knowledge k " +
                "JOIN knowledge_base b ON b.id = k.base_id AND b.status = 2 AND b.deleted = 0 " +
                "WHERE k.deleted = 0 " +
                "AND (k.title LIKE #{like} OR k.summary LIKE #{like} OR k.content LIKE #{like} " +
                "     OR b.name LIKE #{like} OR b.summary LIKE #{like}) " +
                "<if test='type != null and type != \"\"'>AND b.kb_type = #{type}</if>" +
                "</script>")
        long countSearch(@Param("like") String like, @Param("type") String type);

        @Select("<script>" +
                "SELECT k.id, k.base_id AS baseId, k.title, b.kb_type AS kbType, k.summary, " +
                "       b.cover_url AS coverUrl, b.published_at AS publishedAt, " +
                "       b.subscribe_count AS subscribeCount, IFNULL(u.nickname, '佚名') AS authorName " +
                "FROM knowledge k " +
                "JOIN knowledge_base b ON b.id = k.base_id AND b.status = 2 AND b.deleted = 0 " +
                "LEFT JOIN sys_user u ON u.id = k.author_id " +
                "WHERE k.deleted = 0 " +
                "AND (k.title LIKE #{like} OR k.summary LIKE #{like} OR k.content LIKE #{like} " +
                "     OR b.name LIKE #{like} OR b.summary LIKE #{like}) " +
                "<if test='type != null and type != \"\"'>AND b.kb_type = #{type}</if>" +
                " ORDER BY b.subscribe_count DESC, k.id DESC LIMIT #{offset}, #{size}" +
                "</script>")
        List<Map<String, Object>> search(@Param("like") String like, @Param("type") String type,
                                         @Param("offset") long offset, @Param("size") int size);
    }
}
