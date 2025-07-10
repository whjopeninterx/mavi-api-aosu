package com.openinterx.mavi.controller;

import com.openinterx.mavi.config.NacosBaseConfig;
import com.openinterx.mavi.exception.ValidateException;
import com.openinterx.mavi.handler.UserSessionHandler;
import com.openinterx.mavi.pojo.config.VideoHandlerLimitConfig;
import com.openinterx.mavi.pojo.system.UnderstandParams;
import com.openinterx.mavi.service.VideoUnderstandService;
import com.openinterx.mavi.util.FileFetcher;
import com.openinterx.mavi.util.JsonUtils;
import com.openinterx.mavi.util.limit.VideoHandlerLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/understand")
@RequiredArgsConstructor
public class VideoUnderstandController {
    private static final long MAX_FILE_VIDEO_SIZE = 110 * 1024 * 1024; // 110MB
    private static final long MAX_FILE_IMG_SIZE = 2 * 1024 * 1024; // 2MB
    private final VideoUnderstandService videoUnderstandService;
    private NacosBaseConfig nacosConfig;
    public static final String UNDERSTAND_API_SAFE_VIDEO="UNDERSTAND:API:SAFE:VIDEO:";


    @PostMapping("upload")
    public UnderstandParams.UnderstandRes upload(@RequestBody UnderstandParams.UnderstandReq req) {
        final VideoHandlerLimitConfig config = JsonUtils.strToObj(nacosConfig.getTranscriptionLimitConfig(), VideoHandlerLimitConfig.class);
        final String clientId = UserSessionHandler.getClientId();
        final VideoHandlerLimit videoHandlerLimit = new VideoHandlerLimit(config);
        if(!videoHandlerLimit.isAllowed(UNDERSTAND_API_SAFE_VIDEO,clientId)){
            throw new ValidateException("Request has exceeded the limit.");
        }
        // 检查上传参数
        checkUpload(req);
        return videoUnderstandService.upload(req);
    }

    private void checkUpload(UnderstandParams.UnderstandReq req) {
        if (!StringUtils.hasLength(req.getUserPrompt())) {
            throw new ValidateException("userPrompt is not allowed, please provide a user prompt");
        }
        if (!StringUtils.hasLength(req.getSystemPrompt())) {
            throw new ValidateException("systemPrompt is not allowed, please provide a system prompt");
        }
        if (!StringUtils.hasLength(req.getCallback())) {
            throw new ValidateException("callback is not allowed, please provide a callback URL");
        }
        //校验视频
        if (!StringUtils.hasLength(req.getVideoUrl())) {
            throw new ValidateException("videoUrl is not allowed, please upload a video file");
        }
        final FileFetcher.RemoteFileMeta videoFileMeta = FileFetcher.getRemoteFileMeta(req.getVideoUrl());
        final long videoFileSize = videoFileMeta.getContentLength();
        if (videoFileSize > MAX_FILE_VIDEO_SIZE || videoFileSize == -1 || videoFileSize == 0) {
            throw new ValidateException("Video file size exceeds the limit of 110MB");
        }
        if (!FileFetcher.isVideoMimeType(videoFileMeta.getContentType())) {
            throw new ValidateException("videoUrl is not support video type, please upload a video file");
        }
        req.setVideoType(videoFileMeta.getContentType());

        if (req.getPersons() != null && !req.getPersons().isEmpty()) {
            for (UnderstandParams.Person person : req.getPersons()) {
                if (!StringUtils.hasLength(person.getName())) {
                    throw new ValidateException("person name is not null,please provide a name");
                }
                if (StringUtils.hasLength(person.getUrl())) {
                    final FileFetcher.RemoteFileMeta imgFileMeta = FileFetcher.getRemoteFileMeta(person.getUrl());
                    final long imgFileSize = imgFileMeta.getContentLength();
                    if (imgFileSize > MAX_FILE_IMG_SIZE || imgFileSize == -1 || imgFileSize == 0) {
                        throw new ValidateException("Image file size exceeds the limit of 2MB");
                    }

                    if (!FileFetcher.isImageMimeType(imgFileMeta.getContentType())) {
                        throw new ValidateException("person url is not support image type, please upload a image file");
                    }
                    person.setImgType(imgFileMeta.getContentType());
                } else {
                    throw new ValidateException("person url is not null, please upload a image file");
                }
            }
        }

    }


}
