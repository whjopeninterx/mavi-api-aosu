package com.openinterx.mavi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openinterx.mavi.model.UnderstandQueue;
import org.apache.ibatis.annotations.Param;


public interface UnderstandQueueMapper extends BaseMapper<UnderstandQueue> {


    int insertSelective(UnderstandQueue record);

    int updateStatus(@Param("status") String status, @Param("taskId") String taskId,@Param("processing") Boolean processing,@Param("oldProcessing") Boolean oldProcessing);

    int updateResult(@Param("taskId") String taskId,
                      @Param("status") String status,
                      @Param("processing") Boolean processing,
                      @Param("oldProcessing") Boolean oldProcessing,
                      @Param("payload") String payload,
                      @Param("lastError") String lastError);

}