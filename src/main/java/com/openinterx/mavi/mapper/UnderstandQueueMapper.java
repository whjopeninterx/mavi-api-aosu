package com.openinterx.mavi.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openinterx.mavi.model.UnderstandQueue;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UnderstandQueueMapper extends BaseMapper<UnderstandQueue> {
    int deleteByPrimaryKey(Integer id);

    int insert(UnderstandQueue record);

    int insertSelective(UnderstandQueue record);

    UnderstandQueue selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UnderstandQueue record);

    int updateByPrimaryKey(UnderstandQueue record);
}