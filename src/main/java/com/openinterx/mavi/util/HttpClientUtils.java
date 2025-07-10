package com.openInterX.common.util;

import com.openInterX.common.exception.XvuException;
import com.openInterX.common.pojo.response.ModelResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

@Slf4j
public class HttpClientUtils {

    public static final String UTF8 = "UTF-8";
    private final static String CONTENT_TYPE_TEXT_JSON = "text/json";
    private static final Integer CODE=200;
    public static String get(String url) {
        // 配置请求的超时时间等
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000) // 连接超时
                .setSocketTimeout(30000)  // 读取超时
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        HttpGet httpGet = new HttpGet(url);
        String result = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                result = EntityUtils.toString(entity, UTF8);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new XvuException(e);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }

        return result;
    }


    public static String get(String url, Map<String, String> headers, Map<String, String> data) {
        StringBuilder para = new StringBuilder();
        for (String item : data.keySet()) {
            para.append("&");
            para.append(item);
            para.append("=");
            para.append(URLEncoder.encode(data.get(item)));
        }

        if (para.length() > 0) {
            url = url.indexOf("?") > 0 ? url + para : url + "?" + para.substring(1);
        }
        return get(url, headers);
    }


    public static String get(String url, Map<String, String> headers) {
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpGet httpGet = new HttpGet(url);
        String result = null;
        try {
            if (headers != null) {
                for (String item : headers.keySet()) {
                    httpGet.addHeader(item, headers.get(item));
                }
            }
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                result = EntityUtils.toString(entity, UTF8);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error(url, e);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
        return result;
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



    public static String postJson(String urlString, String jsonInputString) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            // 创建 URL 对象
            URL url = new URL(urlString);
            // 打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 发送 JSON 数据
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
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








    public static Flux<ModelResult> postStream(String url, String json) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3600000) // 连接超时
                .setConnectionRequestTimeout(3600000) // 请求超时
                .setSocketTimeout(3600000) // 套接字超时
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        return Flux.create(sink -> {
            Schedulers.boundedElastic().schedule(() -> {
                try {
                    HttpPost postRequest = new HttpPost(url);
                    postRequest.setHeader("Content-Type", "application/json;charset=UTF-8");
                    postRequest.setHeader("Accept","text/event-stream; charset=utf-8");
                    StringEntity se = new StringEntity(json, "UTF-8");
                    postRequest.setEntity(se);
                    CloseableHttpResponse response = httpClient.execute(postRequest);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                                String data;
                                while ((data = bufferedReader.readLine()) != null) {
                                    if(StringUtil.isNotEmpty(data)){
                                        data = data.substring(5);
                                        sink.next(JsonUtils.strToObj(data,ModelResult.class));
                                    }
                                }
                                sink.complete();
                            }
                        } else {
                            final String errorMessage = "Error: Received entity is null " + JsonUtils.objToStr(entity);
                            throw new XvuException(errorMessage);
                        }
                    } else {
                        // 处理非200响应
                        final String errorMessage = "Error: Received status code " + statusCode;
                        throw new XvuException(errorMessage);
                    }
                } catch (Exception e) {
                    String errorMessage = "Exception occurred: " + e.getMessage();
//            return new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8));
                    throw new XvuException(errorMessage);
                }
            });
        });
        //  return new ByteArrayInputStream(new byte[0]); // 返回空输入流
    }


    public static Flux<String> postStreamStr(String url, String json) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3600000) // 连接超时
                .setConnectionRequestTimeout(3600000) // 请求超时
                .setSocketTimeout(3600000) // 套接字超时
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        return Flux.create(sink -> {
            Schedulers.boundedElastic().schedule(() -> {
                try {
                    HttpPost postRequest = new HttpPost(url);
                    postRequest.setHeader("Content-Type", "application/json;charset=UTF-8");
                    postRequest.setHeader("Accept","text/event-stream; charset=utf-8");
                    StringEntity se = new StringEntity(json, "UTF-8");
                    postRequest.setEntity(se);
                    CloseableHttpResponse response = httpClient.execute(postRequest);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                                String data;
                                while ((data = bufferedReader.readLine()) != null) {
                                    if(StringUtil.isNotEmpty(data)){
                                        data = data.substring(5);
                                        sink.next(data);
                                    }
                                }
                                sink.complete();
                            }
                        } else {
                            final String errorMessage = "Error: Received entity is null " + JsonUtils.objToStr(entity);
                            throw new XvuException(errorMessage);
                        }
                    } else {
                        // 处理非200响应
                        final String errorMessage = "Error: Received status code " + statusCode;
                        throw new XvuException(errorMessage);
                    }
                } catch (Exception e) {
                    String errorMessage = "Exception occurred: " + e.getMessage();
//            return new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8));
                    throw new XvuException(errorMessage);
                }
            });
        });
        //  return new ByteArrayInputStream(new byte[0]); // 返回空输入流
    }

}
