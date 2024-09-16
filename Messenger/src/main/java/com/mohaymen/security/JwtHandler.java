package com.mohaymen.security;

import io.jsonwebtoken.Jwts;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class JwtHandler {

    private static final String SECRET_KEY = "2BONtKNaqHAMhbigbtitKmQRDf3iIysttFlJ8BQ2ed5uaErlzUMS0Kcq66p5rDko+BRT2pfCcTSS3CdeZKZaVapj3p2LztPU7yrlJrVZOMo=";

    @Setter
    private static String VERSION_KEY;

    private static String key() {
        return SECRET_KEY + VERSION_KEY;
    }

    public static String generateAccessToken(Long accountId) {
        Date expirationTime =
                Date.from(Instant.now().plus(7, ChronoUnit.DAYS));

        String accessToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("id", accountId)
                .setExpiration(expirationTime)
                .compact();

        String signature = encode(accessToken.substring(0, accessToken.length() - 1));

        return accessToken + signature;
    }

    public static Long getIdFromAccessToken(String accessToken) throws Exception {
        if(!validateAccessToken(accessToken))
            throw new Exception("Token signature is not valid");
        Long result;
        try {
            result = decodeAccessToken(accessToken);
        } catch (JSONException e) {
            throw new Exception("Token data is not valid");
        }
        return result;
    }

    private static Boolean validateAccessToken(String accessToken) {
        String[] chunks = accessToken.split("\\.");
        if(chunks.length != 3){
            return false;
        }
        if(!encode(chunks[0] + "." + chunks[1]).equals(chunks[2])){
            return false;
        }
        try {
            return getExpToken(accessToken) >= Instant.now().getEpochSecond();
        } catch (JSONException e) {
            return false;
        }
    }

    private static Long getExpToken(String accessToken) throws JSONException {
        String[] chunks = accessToken.split("\\.");
        JSONObject payload = new JSONObject(decode(chunks[1]));
        return ((Number) payload.get("exp")).longValue();
    }

    private static Long decodeAccessToken(String accessToken) throws JSONException {
        String[] chunks = accessToken.split("\\.");
        JSONObject payload = new JSONObject(decode(chunks[1]));
        return ((Number) payload.get("id")).longValue();
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    @SneakyThrows
    private static String encode(String data) {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

}
