package com.openinterx.mavi.util;
import com.openinterx.mavi.exception.XvuException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
public class MD5Utils {

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new XvuException("MD5 加密错误！");
        }

        // 将字符串转换为指定编码的字节数组
        byte[] byteArray;
        byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}
