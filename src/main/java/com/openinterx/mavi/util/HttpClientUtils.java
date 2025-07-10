package com.openinterx.mavi.util;


import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Slf4j
public class HttpClientUtils {
    /**
     * 发送 POST JSON 请求（无请求头）
     *
     * @param url     请求地址
     * @param json 请求体参数（Map 会自动转为 JSON）
     * @return 响应体字符串
     */
    public static String postJson(String url, String json) {


        try (HttpResponse response = HttpRequest.post(url)
                .body(json)
                .contentType(ContentType.JSON.getValue()) // 设置 Content-Type: application/json
                .timeout(30000)
                .execute()) {
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("POST 请求失败: " + e.getMessage(), e);
        }
    }

    public static String postJson(String urlString, String json, Map<String, String> headerMap) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            // 创建 URL 对象
            URL url = new URL(urlString);
            // 打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法
            connection.setRequestMethod("POST");
            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/json");

            // 设置额外的请求头
            for (String headerKey : headerMap.keySet()) {
                connection.setRequestProperty(headerKey, headerMap.get(headerKey));
            }

            // 设置允许输出数据
            connection.setDoOutput(true);

            // 如果 jsonInputString 是 null 或空字符串，则使用一个空的 JSON 对象 "{}"
            if (json == null || json.isEmpty()) {
                json = "{}"; // 使用一个空的 JSON 对象
            }
            // 发送 JSON 数据
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HttpURLConnection failed with response code: " + responseCode);
            }

            // 读取响应内容
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

        } catch (Exception e) {
            System.err.println("Error occurred while sending POST request: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();  // 关闭连接
            }
        }

        return response.toString();  // 返回响应内容
    }




}
