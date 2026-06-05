package com.ainovel.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainovel.backend.model.entity.Novel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NovelMapper extends BaseMapper<Novel> {
}
