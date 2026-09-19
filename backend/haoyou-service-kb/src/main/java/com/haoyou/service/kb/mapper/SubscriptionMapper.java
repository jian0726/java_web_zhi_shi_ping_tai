package com.haoyou.service.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoyou.service.kb.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {

    @Select("SELECT COUNT(*) FROM subscription WHERE user_id = #{userId} AND base_id = #{baseId}")
    long countByUserAndBase(@Param("userId") Long userId, @Param("baseId") Long baseId);
}
