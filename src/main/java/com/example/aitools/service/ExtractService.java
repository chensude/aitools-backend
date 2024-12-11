package com.example.aitools.service;

import com.example.aitools.model.response.ExtractResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ExtractService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private final OkHttpClient client;

    public ExtractService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)))
                .build();
    }

    private String identifyPlatform(String url) {
        if (url.contains("douyin.com")) return "抖音";
        if (url.contains("xiaohongshu.com")) return "小红书";
        if (url.contains("kuaishou.com")) return "快手";
        throw new UnsupportedOperationException("Unsupported platform");
    }

    @Cacheable(value = "contentCache", key = "#url")
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ExtractResponse extractContent(String url) {
        String realUrl = getRealUrl(url);
        String platform = identifyPlatform(realUrl);

        try {
            switch (platform) {
                case "抖音":
                    return extractFromDouyin(realUrl);
                case "小红书":
                    return extractFromXiaohongshu(realUrl);
                case "快手":
                    return extractFromKuaishou(realUrl);
                default:
                    throw new UnsupportedOperationException("Unsupported platform");
            }
        } catch (Exception e) {
            log.error("Extraction failed: {}", e.getMessage());
            throw new RuntimeException("Extraction failed: " + e.getMessage());
        }
    }

    private String getRealUrl(String url) {
        if (url.contains("v.douyin.com")) {
            return getDouyinRealUrl(url);
        }
        if (url.contains("xhslink.com")) {
            return getXiaohongshuRealUrl(url);
        }
        if (url.contains("v.kuaishou.com")) {
            return getKuaishouRealUrl(url);
        }
        return url;
    }

    private String getDouyinRealUrl(String url) {
        return getRedirectUrl(url);
    }

    private String getXiaohongshuRealUrl(String url) {
        return getRedirectUrl(url);
    }

    private String getKuaishouRealUrl(String url) {
        return getRedirectUrl(url);
    }

    private String getRedirectUrl(String input) {
        // 使用正则表达式提取链接
        Pattern pattern = Pattern.compile("https?://[\\w./-]+");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String url = matcher.group();

            if (url.contains("v.douyin.com")||url.contains("xhslink.com")||url.contains("v.kuaishou.com")) {
                return url;
            }
        } else {
            throw new IllegalArgumentException("不合法的链接");
        }
        throw new IllegalArgumentException("不合法的链接");
    }


    private ExtractResponse extractFromDouyin(String url) {
        try {
            String videoId = extractDouyinVideoId(url);
            String apiUrl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + videoId;

            JSONObject data = fetchJsonFromApi(apiUrl)
                    .getJSONArray("item_list")
                    .getJSONObject(0);

            ExtractResponse result = new ExtractResponse();
            result.setPlatform("抖音");
            result.setAuthor(data.getJSONObject("author").getString("nickname"));
            result.setContent(data.getString("desc"));
            result.setLikes(data.getJSONObject("statistics").getInteger("digg_count"));
            result.setComments(data.getJSONObject("statistics").getInteger("comment_count"));
            result.setPublishTime(formatTimestamp(data.getLong("create_time")));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Douyin content");
        }
    }

    private ExtractResponse extractFromXiaohongshu(String url) {
        try {
            String noteId = extractXiaohongshuNoteId(url);
            String apiUrl = "https://www.xiaohongshu.com/api/sns/v1/note/" + noteId;

            JSONObject data = fetchJsonFromApi(apiUrl, "your_cookie_here")
                    .getJSONObject("data");

            ExtractResponse result = new ExtractResponse();
            result.setPlatform("小红书");
            result.setAuthor(data.getJSONObject("user").getString("nickname"));
            result.setContent(data.getString("desc"));
            result.setLikes(data.getInteger("likes"));
            result.setComments(data.getInteger("comments"));
            result.setPublishTime(formatTimestamp(data.getLong("time")));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Xiaohongshu content");
        }
    }

    private ExtractResponse extractFromKuaishou(String url) {
        try {
            String videoId = extractKuaishouVideoId(url);
            String apiUrl = "https://m.gifshow.com/rest/wd/photo/info?photoId=" + videoId;

            JSONObject data = fetchJsonFromApi(apiUrl)
                    .getJSONObject("photo");

            ExtractResponse result = new ExtractResponse();
            result.setPlatform("快手");
            result.setAuthor(data.getString("userName"));
            result.setContent(data.getString("caption"));
            result.setLikes(data.getInteger("likeCount"));
            result.setComments(data.getInteger("commentCount"));
            result.setPublishTime(formatTimestamp(data.getLong("timestamp")));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Kuaishou content");
        }
    }

    private String extractDouyinVideoId(String url) {
        return extractIdFromUrl(url, "/v.douyin.com/(\\d+)");
    }

    private String extractXiaohongshuNoteId(String url) {
        return extractIdFromUrl(url, "/discovery/item/(\\w+)");
    }

    private String extractKuaishouVideoId(String url) {
        return extractIdFromUrl(url, "/short-video/(\\w+)");
    }

    private String extractIdFromUrl(String url, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Invalid URL");
    }

    private JSONObject fetchJsonFromApi(String url) throws IOException {
        return fetchJsonFromApi(url, null);
    }

    private JSONObject fetchJsonFromApi(String url, String cookie) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT);

        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return JSON.parseObject(response.body().string());
        }
    }

    private String formatTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}