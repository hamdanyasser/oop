package com.example.finalproject.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.TokenExpiredException;

import java.util.Date;

public class JwtService {
    private static final String SECRET = "your-secret-key";
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24h

    public static String issueToken(int userId, String role, String email) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static String issueToken(int userId, String role) {
        return issueToken(userId, role, "");
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token); // throws if expired/invalid
    }

    public static boolean isExpired(String token) {
        try {
            DecodedJWT jwt = verify(token); // throws if expired
            Date exp = jwt.getExpiresAt();
            return exp == null || exp.before(new Date());
        } catch (TokenExpiredException ex) {
            return true;
        } catch (Exception ex) {
            // invalid token = treat as expired/invalid
            return true;
        }
    }

    public static int getUserId(String token) {
        try { return verify(token).getClaim("userId").asInt(); } catch (Exception e) { return -1; }
    }
    public static String getRole(String token) {
        try { return verify(token).getClaim("role").asString(); } catch (Exception e) { return null; }
    }
    public static String getEmail(String token) {
        try { return verify(token).getClaim("email").asString(); } catch (Exception e) { return null; }
    }
}
