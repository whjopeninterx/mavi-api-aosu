package com.openinterx.mavi.util;

import com.openinterx.mavi.pojo.aosu.UnderstandCallbackParam;
import com.openinterx.mavi.pojo.common.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AosuUtils {

    public static Boolean callback(String url, UnderstandCallbackParam.CallBackReq param) {
        try {
            final Result result = JsonUtils.strToObj(HttpClientUtils.postJson(url, JsonUtils.objToStr(param)),Result.class);
            if(result!=null&&result.getCode()==0){
                return true;
            }else {
                log.error("AosuUtils callback url:{},req: {},res:{}", url,JsonUtils.objToStr(param), JsonUtils.objToStr(result));
                return false;
            }
        }catch (Exception e){
            log.error("AosuUtils callback url:{},req: {}", url,JsonUtils.objToStr(param), e);
            return false;
        }
    }
}
