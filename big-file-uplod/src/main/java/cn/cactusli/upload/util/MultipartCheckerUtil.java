package cn.cactusli.upload.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Package: cn.cactusli.upload.util
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 15:32
 * @Github https://github.com/lixuanfengs
 */
public class MultipartCheckerUtil {

    public static boolean isMultipartContent(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
}
