package com.openinterx.mavi.service.Impl;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.openinterx.mavi.exception.ValidateException;
import com.openinterx.mavi.handler.UserSessionHandler;
import com.openinterx.mavi.mapper.UnderstandQueueMapper;
import com.openinterx.mavi.model.UnderstandQueue;
import com.openinterx.mavi.pojo.aosu.UnderstandCallbackParam;
import com.openinterx.mavi.pojo.common.TokenRes;
import com.openinterx.mavi.pojo.system.UnderstandParams;
import com.openinterx.mavi.service.VideoUnderstandService;
import com.openinterx.mavi.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoUnderstandServiceImpl implements VideoUnderstandService {

    private final Executor virtualThreadExecutor;
    private final UnderstandQueueMapper taskQueueMapper;

    @Override
    @Transactional
    public UnderstandParams.UnderstandRes upload(UnderstandParams.UnderstandReq req) {
        // 下载视频并且上传
        // System.out.println(taskQueueMapper.selectByPrimaryKey(1));
        try {
            final List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 上传主视频
            final InputStream videoStream = new URL(req.getVideoUrl()).openStream();
            CompletableFuture<Void> videoFuture = CompletableFuture.runAsync(() -> {
                final String key = req.getVideoUrl().substring(req.getVideoUrl().lastIndexOf('/') + 1);
                FileStorageUtils.UploadResponse uploadResponse =
                        FileStorageUtils.gcs_upload(videoStream, key, UserSessionHandler.getClientId());
                req.setVideoUrl(uploadResponse.getUrl());
            }, virtualThreadExecutor);
            futures.add(videoFuture);

            // 上传人物图像
            if (req.getPersons() != null && !req.getPersons().isEmpty()) {
                for (UnderstandParams.Person person : req.getPersons()) {
                    final InputStream inputStream = new URL(person.getUrl()).openStream();
                    CompletableFuture<Void> personFuture = CompletableFuture.runAsync(() -> {
                        final String key = person.getUrl().substring(person.getUrl().lastIndexOf('/') + 1);
                        FileStorageUtils.UploadResponse uploadResponse =
                                FileStorageUtils.gcs_upload(inputStream, key, UserSessionHandler.getClientId());
                        person.setUrl(uploadResponse.getUrl());
                    }, virtualThreadExecutor);
                    futures.add(personFuture);
                }
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            //等待所有携程结束 插入一条数据
            final UnderstandQueue understandQueue = new UnderstandQueue();
            understandQueue.setTaskId(UuidUtils.generateUuid());
            understandQueue.setParam(JsonUtils.objToStr(req));
            understandQueue.setStatus("PENDING");
            understandQueue.setCallbackUrl(req.getCallback());
            understandQueue.setProcessing(false);
            understandQueue.setRetryCount(0);
            understandQueue.setMaxRetry(3);
            understandQueue.setUpdatedAt(System.currentTimeMillis());
            taskQueueMapper.insertSelective(understandQueue);
            // 事务外提交一个任务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 在提交后的回调中开启一个新事务
                    virtualThreadExecutor.execute(() -> {
                        UnderStandIndex(req,understandQueue.getTaskId());
                    });
                }
            });
        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage());
            throw new ValidateException("Failed to upload file", e);
        }

        return null;
    }

    @Override
    @Transactional
    public void UnderStandIndex(UnderstandParams.UnderstandReq req,String taskId) {
        // 处理视频理解逻辑
        final int count= taskQueueMapper.updateStatus("PROCESSING", taskId, true, false);
        if (count == 0) {
            log.warn("Task {} is already being processed or does not exist.", taskId);
            return;
        }
        final UnderstandCallbackParam.CallBackReq param = new UnderstandCallbackParam.CallBackReq();
        param.setTaskId(taskId);
        String lastError = null;
        try {
            GeminiUtils.GeminiResponse geminiResponse;
            if (req.getPersons() != null && !req.getPersons().isEmpty()) {
                geminiResponse = GeminiUtils.understandVideoWithImg(req);
            } else {
                geminiResponse = GeminiUtils.understandVideoNoImg(req);
            }
            final UnderstandCallbackParam.CallBackData callBackData = new UnderstandCallbackParam.CallBackData();
            callBackData.setText(geminiResponse.getText());
            param.setData(callBackData);
            param.setStatus(0);
            final TokenRes token = new TokenRes();
            token.setInput(geminiResponse.getInput());
            token.setOutput(geminiResponse.getOutput());
            token.setTotal(geminiResponse.getTotal());
            param.setToken(token);
        }catch (Exception e) {
            param.setStatus(-1);
            lastError= e.getMessage();
        }
        final Boolean flag = AosuUtils.callback(req.getCallback(), param);
        if(flag){
            taskQueueMapper.updateResult(taskId,"COMPLETED", false, true,JsonUtils.objToStr(param),lastError);
        }else{
            taskQueueMapper.updateResult(taskId,"FAILED", false, true,JsonUtils.objToStr(param),lastError);
        }
    }
}
