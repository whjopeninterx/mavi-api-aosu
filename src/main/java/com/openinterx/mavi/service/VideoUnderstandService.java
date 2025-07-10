package com.openinterx.mavi.service;

import com.openinterx.mavi.pojo.system.UnderstandParams;

public interface VideoUnderstandService {

    UnderstandParams.UnderstandRes upload(UnderstandParams.UnderstandReq req);

    void UnderStandIndex(UnderstandParams.UnderstandReq req,String taskId);
}
