package is.meniga.mobileandroidstarter.auth.tokens;

import android.util.Base64;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 19.2.2018.
 */
public abstract class Token {

    private DateTime expiry;
    private final String token;

    public Token(String token) {
        this.token = token;
        parse();
    }

    private void parse() {
        String[] accessTokenParts = token.split("\\.");
        String accessPayload = null;
        try {
            accessPayload = new String(Base64.decode(accessTokenParts[1].getBytes("UTF-8"), Base64.DEFAULT), "UTF-8");
            // Parse payload
            JSONObject accessJson = new JSONObject(accessPayload);
            // Save expiry time
            expiry = new DateTime(accessJson.getLong("exp") * 1000L);
        } catch (JSONException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isExpired() {
        return token == null || DateTime.now().isAfter(expiry);
    }

    public DateTime getExpiry() {
        return expiry;
    }

    public String getToken() {
        return token;
    }
}
