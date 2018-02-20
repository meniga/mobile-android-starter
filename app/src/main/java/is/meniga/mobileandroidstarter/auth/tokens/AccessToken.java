package is.meniga.mobileandroidstarter.auth.tokens;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 19.2.2018.
 */
public class AccessToken extends Token {
    public AccessToken(String payload) throws Exception {
        super(payload);
    }

    @Override
    public String toString() {
        return super.getToken();
    }
}
