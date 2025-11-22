package com.example.finalproject.security;

public class Session {
    private static String token;

    public static void setToken(String t) { token = t; }
    public static String getToken() { return token; }
    public static void clear() { token = null; }

    public static boolean isAuthenticated() {
        if (token == null) return false;
        boolean expiredOrInvalid = JwtService.isExpired(token);
        if (expiredOrInvalid) token = null;
        return !expiredOrInvalid;
    }

    public static int getUserId() {
        return token == null ? -1 : JwtService.getUserId(token);
    }

    public static String getUserRole() {
        return token == null ? null : JwtService.getRole(token);
    }

    public static String getUserEmail() {
        return token == null ? null : JwtService.getEmail(token);
    }
}
