package com.openinterx.mavi.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openinterx.mavi.exception.XvuException;
import com.openinterx.mavi.pojo.system.UnderstandParams;

import com.openinterx.mavi.util.cache.GoogleCloudAPITokenCache;
import jodd.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GeminiUtils {


    private static final String projectId = "gen-lang-client-0057517563";
    private static final String location = "us-central1";
    private static final String gemini_1_5_flash = "gemini-1.5-flash";
    private static final String gemini_2_5_flash = "gemini-2.5-flash";
    private static final String API_URL = "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent";

    private static final String REID_PROMPT_TEMPLATE = """
            You are a highly precise surveillance analyst specialized in person re-identification and detailed characteristic description. Leave the field as an empty string if the feature is not discernible.
            
            **JSON Output Schema:**
            
            ```json
            {
              "person_analysis": [
                {
                  "id": "<The filename ID>",
                  "appearance": {
                    "gender": "<Male, Female, or Unsure>",
                    "age_group": "<Child, Teenager, Adult, Senior, or Unsure>",
                    "physical_build": "<Description of build, e.g., slim, athletic, heavy>",
                    "hair": "<Color and style, e.g., short black, long blonde, bald>",
                    "face": "<Detailed description of facial features if 'Unidentified' and visible, e.g., round face, distinct nose, wearing glasses>",
                    "eye_color": "<Color, if clearly visible, otherwise 'Unsure'>"
                  },
                  "clothing": {
                    "top": "<Description, e.g., blue t-shirt, striped polo>",
                    "bottom": "<Description, e.g., jeans, khaki shorts>",
                    "outerwear": "<Description, e.g., black jacket, no jacket>",
                    "headwear": "<Description or None>",
                    "footwear": "<Description, e.g., sneakers, boots>",
                    "accessories": "<Description, e.g., backpack, watch, None>"
                  }
                }
              ]
            }
            """;

    private static final String IMG_PROMPT = """
            --- Start of Reference Images ---
            
            Below are reference images of known individuals. For each image, you will be provided with an ID 
            followed by the image itself. Analyze these images to create detailed profiles for each person, 
            including their physical appearance, build, hair, eyes, and any distinguishing features. Store these 
            profiles for later use in identifying individuals in the video footage. Here are the reference individuals:
            """;

    private static final String IMAGE_REF_ID = "Reference ID: ";

    private static final String IMAGE_END = "--- End of Reference Images --- \\n";

    private static final String ANALYSIS = "--- Previous Person Analysis ---\\n";
    private static final String ANALYSIS_END = "--- End of Previous Person Analysis --- \\n";

    private static final String VIDEO_PROMPT = "**Identity:** If a person in the video matches a Person Analysis, you **MUST** use their `id` (e.g., `JohnSmith`) in all relevant fields (`description`, `title`, `summary`, `objects.id`).\\n";


    private static final String VIDEO_START = "--- Start of video ---";
    private static final String VIDEO_END = "--- End of video ---";


    public static GeminiResponse understandVideoNoImg(UnderstandParams.UnderstandReq req) {
        try {
            final String token = getToken();
            final String model = gemini_1_5_flash;
            final String endpoint = String.format(
                    API_URL,
                    location, projectId, location, model);

            final GeminiParam.GeminiChatReq requestBody = buildRequestBodyNoImg(req);
            final Map<String, String> param = new HashMap<>();
            param.put("Authorization", "Bearer " + token);
            final GeminiParam.GeminiChatResponse response = JsonUtils.strToObj(HttpClientUtils.postJson(endpoint, JsonUtils.objToStr(requestBody), param), GeminiParam.GeminiChatResponse.class);

            if (response != null && CollectionUtil.isNotEmpty(response.getCandidates())) {
                String text = null;
                for (GeminiParam.Candidate candidate : response.getCandidates()) {
                    final GeminiParam.Content content = candidate.getContent();
                    text = content.getParts().stream().map(GeminiParam.Part::getText).collect(Collectors.joining());
                }
                if (StringUtil.isNotEmpty(text)) {
                    final GeminiParam.UsageMetadata usageMetadata = response.getUsageMetadata();
                    if (usageMetadata != null) {
                        log.info("Google Gemini API usage metadata: {}", JsonUtils.objToStr(usageMetadata));
                    }
                    final GeminiResponse geminiResponse = new GeminiResponse();
                    geminiResponse.setText(text);
                    geminiResponse.setInput(usageMetadata != null ? usageMetadata.getPromptTokenCount() : 0);
                    geminiResponse.setOutput(usageMetadata != null ? usageMetadata.getCandidatesTokenCount() : 0);
                    geminiResponse.setTotal(usageMetadata != null ? usageMetadata.getTotalTokenCount() : 0);
                    return geminiResponse;
                } else {
                    throw new XvuException("No text content found in the response.");
                }

            }
        } catch (Exception e) {
            throw new XvuException("Request error Contact your administrator: " + e);
        }
        return null;

    }

    private static GeminiParam.GeminiChatReq buildRequestBodyNoImg(UnderstandParams.UnderstandReq req) {
        // 组装系统prompt
        final GeminiParam.ContentReq systemInstruction = new GeminiParam.ContentReq();
        final GeminiParam.PartReq partReq = new GeminiParam.PartReq();
        partReq.setText(req.getSystemPrompt());
        systemInstruction.setParts(List.of(partReq));
        // 组装用户prompt
        final GeminiParam.ContentReq contentReq = new GeminiParam.ContentReq();
        final GeminiParam.PartReq videoStart = new GeminiParam.PartReq();
        videoStart.setText(VIDEO_START);
        contentReq.getParts().add(videoStart);

        final GeminiParam.FileData fileData = new GeminiParam.FileData();
        fileData.setFileUri(req.getVideoUrl());
        fileData.setMimeType(req.getVideoType());
        final GeminiParam.PartReq filePartReq = new GeminiParam.PartReq();
        filePartReq.setFileData(fileData);
        contentReq.getParts().add(filePartReq);
        final GeminiParam.PartReq videoEnd = new GeminiParam.PartReq();
        videoEnd.setText(VIDEO_END);
        contentReq.getParts().add(videoEnd);

        final GeminiParam.PartReq userPrompt = new GeminiParam.PartReq();
        userPrompt.setText(req.getUserPrompt());
        contentReq.getParts().add(userPrompt);

        final GeminiParam.GeminiChatReq geminiChatReq = new GeminiParam.GeminiChatReq();
        geminiChatReq.setContents(Collections.singletonList(contentReq));
        geminiChatReq.setSystemInstruction(systemInstruction);
        return geminiChatReq;

    }

    public static GeminiResponse understandVideoWithImg(UnderstandParams.UnderstandReq req) {
        try {
            final String token = getToken();
            final String model = gemini_2_5_flash;
            final String endpoint = String.format(
                    API_URL,
                    location, projectId, location, model);
            final GeminiParam.GeminiChatReq requestBody = buildStage1Req(req);
            final Map<String, String> param = new HashMap<>();
            param.put("Authorization", "Bearer " + token);
            final GeminiParam.GeminiChatResponse response = JsonUtils.strToObj(HttpClientUtils.postJson(endpoint, JsonUtils.objToStr(requestBody), param), GeminiParam.GeminiChatResponse.class);

            if (response != null && CollectionUtil.isNotEmpty(response.getCandidates())) {
                String text = null;
                for (GeminiParam.Candidate candidate : response.getCandidates()) {
                    final GeminiParam.Content content = candidate.getContent();
                    text = content.getParts().stream().map(GeminiParam.Part::getText).collect(Collectors.joining());
                }
                if (StringUtil.isNotEmpty(text)) {
                    final GeminiParam.UsageMetadata setp1 = response.getUsageMetadata();
                    if (setp1 != null) {
                        log.info(" understandVideoWithImg Google Gemini API usage setp1 metadata: {}", JsonUtils.objToStr(setp1));
                    }
                    final GeminiResponse geminiResponse = new GeminiResponse();
                    geminiResponse.setInput(setp1 != null ? setp1.getPromptTokenCount() : 0);
                    geminiResponse.setOutput(setp1 != null ? setp1.getCandidatesTokenCount() : 0);
                    geminiResponse.setTotal(setp1 != null ? setp1.getTotalTokenCount() : 0);
                    // 组装第二阶段请求
                    final GeminiParam.GeminiChatReq request2Body = buildStep2Req(req, text);
                    final GeminiParam.GeminiChatResponse response2 = JsonUtils.strToObj(HttpClientUtils.postJson(endpoint, JsonUtils.objToStr(request2Body), param), GeminiParam.GeminiChatResponse.class);
                    if (response2 != null && CollectionUtil.isNotEmpty(response2.getCandidates())) {
                        text = null;
                        for (GeminiParam.Candidate candidate : response2.getCandidates()) {
                            final GeminiParam.Content content = candidate.getContent();
                            text = content.getParts().stream().map(GeminiParam.Part::getText).collect(Collectors.joining());
                        }
                    }

                    geminiResponse.setText(text);
                    final GeminiParam.UsageMetadata setp2 = response2.getUsageMetadata();
                    if (setp2 != null) {
                        log.info(" understandVideoWithImg Google Gemini API usage setp2 metadata: {}", JsonUtils.objToStr(setp2));
                        geminiResponse.setInput(geminiResponse.getInput() + setp2.getPromptTokenCount());
                        geminiResponse.setOutput(geminiResponse.getOutput() + setp2.getCandidatesTokenCount());
                        geminiResponse.setTotal(geminiResponse.getTotal() + setp2.getTotalTokenCount());
                    }

                    return geminiResponse;
                } else {
                    throw new XvuException("No understandVideoWithImg text content found in the response.");
                }

            }
        } catch (Exception e) {
            throw new XvuException("Request  understandVideoWithImg error Contact your administrator: " + e);
        }
        return null;
    }



    private static GeminiParam.GeminiChatReq buildStep2Req(UnderstandParams.UnderstandReq req, String step1) {
        // 组装系统prompt
        final GeminiParam.ContentReq systemInstruction = new GeminiParam.ContentReq();
        final GeminiParam.PartReq partReq = new GeminiParam.PartReq();
        partReq.setText(req.getSystemPrompt());
        systemInstruction.setParts(List.of(partReq));
        //组装用户prompt
        final GeminiParam.ContentReq contentReq = new GeminiParam.ContentReq();
        final GeminiParam.PartReq analysisStart = new GeminiParam.PartReq();
        analysisStart.setText(ANALYSIS);
        contentReq.getParts().add(analysisStart);

        final GeminiParam.PartReq stepOne= new GeminiParam.PartReq();
        stepOne.setText(step1);
        contentReq.getParts().add(stepOne);

        final GeminiParam.PartReq analysisEnd = new GeminiParam.PartReq();
        analysisEnd.setText(ANALYSIS_END);
        contentReq.getParts().add(analysisEnd);

        final GeminiParam.PartReq videoPrompt = new GeminiParam.PartReq();
        videoPrompt.setText(VIDEO_PROMPT);
        contentReq.getParts().add(videoPrompt);
        final GeminiParam.PartReq videoFile= new GeminiParam.PartReq();
        final GeminiParam.FileData fileData = new GeminiParam.FileData();
        fileData.setFileUri(req.getVideoUrl());
        fileData.setMimeType(req.getVideoType());
        videoFile.setFileData(fileData);
        contentReq.getParts().add(videoFile);


        final GeminiParam.PartReq userPrompt = new GeminiParam.PartReq();
        userPrompt.setText(req.getUserPrompt());
        contentReq.getParts().add(userPrompt);
        final GeminiParam.GeminiChatReq geminiChatReq = new GeminiParam.GeminiChatReq();
        geminiChatReq.setSystemInstruction(systemInstruction);
        geminiChatReq.setContents(Collections.singletonList(contentReq));
        return geminiChatReq;
    }

    private static GeminiParam.GeminiChatReq buildStage1Req(UnderstandParams.UnderstandReq req) {
        // 组装模板prompt
        final GeminiParam.ContentReq contentReq = new GeminiParam.ContentReq();
        final GeminiParam.PartReq imgPromptTemp = new GeminiParam.PartReq();
        imgPromptTemp.setText(IMG_PROMPT);
        contentReq.getParts().add(imgPromptTemp);

        // 组装图片
        for (UnderstandParams.Person person : req.getPersons()) {
            final GeminiParam.FileData fileData = new GeminiParam.FileData();
            fileData.setFileUri(person.getUrl());
            fileData.setMimeType(person.getImgType());
            final GeminiParam.PartReq filePartReq = new GeminiParam.PartReq();
            filePartReq.setFileData(fileData);
            contentReq.getParts().add(filePartReq);
            final GeminiParam.PartReq partReq = new GeminiParam.PartReq();
            partReq.setText(IMAGE_REF_ID + person.getName());
            contentReq.getParts().add(partReq);
        }
        final GeminiParam.PartReq imgEnd = new GeminiParam.PartReq();
        imgEnd.setText(IMAGE_END);
        contentReq.getParts().add(imgEnd);

        final GeminiParam.PartReq aiPrompt = new GeminiParam.PartReq();
        aiPrompt.setText(REID_PROMPT_TEMPLATE);
        contentReq.getParts().add(aiPrompt);
        final GeminiParam.GeminiChatReq stage1Req = new GeminiParam.GeminiChatReq();
        stage1Req.setContents(Collections.singletonList(contentReq));
        return stage1Req;
    }


    private static String getToken() {
        return GoogleCloudAPITokenCache.getGoogleApiToken();
    }

    public static class GeminiParam {
        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class GeminiChatReq {
            private List<ContentReq> contents;
            private ContentReq systemInstruction;
        }

        @Getter
        @Setter
        public static class FileData {
            @JsonProperty("mime_type")
            private String mimeType = "image/jpeg";
            @JsonProperty("file_uri")
            private String fileUri;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Getter
        @Setter
        public static class PartReq {
            @JsonProperty("file_data")
            private FileData fileData;
            @JsonProperty("text")
            private String text;

        }

        @Getter
        @Setter
        public static class ContentReq {
            private String role = "user";
            private List<PartReq> parts = new ArrayList<>();

        }


        @Getter
        @Setter
        public static class GeminiChatResponse {
            private List<Candidate> candidates;
            private UsageMetadata usageMetadata;
            private String modelVersion;
            private String createTime;
            private String responseId;
        }

        @Getter
        @Setter
        public static class Candidate {
            private Content content;

        }

        @Getter
        @Setter
        public static class Content {
            private String role;
            private List<Part> parts;
        }

        @Getter
        @Setter
        public static class Part {
            private String text;
        }

        @Getter
        @Setter
        public static class UsageMetadata {
            private Integer totalTokenCount;
            private Integer promptTokenCount;
            private Integer candidatesTokenCount;
            private List<PromptTokensDetail> promptTokensDetails;
            private List<PromptTokensDetail> candidatesTokensDetails;

        }

        @Getter
        @Setter
        public static class PromptTokensDetail {
            private String modality;
            private Integer tokenCount;
        }
    }
    @Getter
    @Setter
    public static class GeminiResponse {
        public String text;
        private Integer input;
        private Integer output;
        private Integer total;
    }


}
