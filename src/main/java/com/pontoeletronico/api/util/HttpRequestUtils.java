package com.pontoeletronico.api.util;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    public static String obterIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        var xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String obterUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        var userAgent = request.getHeader("User-Agent");
        return userAgent;
    }
}
