package is.meniga.mobileandroidstarter.auth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.meniga.sdk.providers.tasks.Continuation;
import com.meniga.sdk.providers.tasks.Task;
import com.meniga.sdk.providers.tasks.TaskCompletionSource;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import is.meniga.mobileandroidstarter.BuildConfig;
import is.meniga.mobileandroidstarter.R;
import is.meniga.mobileandroidstarter.auth.tokens.AccessToken;
import is.meniga.mobileandroidstarter.auth.tokens.RefreshToken;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 19.2.2018.
 */
public class AuthService implements Authenticator, Interceptor {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int RESPONSE_CODE_BAD_REQUEST = 400;
    private static final int RESPONSE_CODE_UNAUTHORIZED = 401;
    private static final int RESPONSE_CODE_SERVER_MAINTENANCE = 503;
    private static final int RESPONSE_CODE_SERVER_NOT_FOUND = 404;

    private static final Object lock = new Object();
    private static AuthService service;

    private final String HEADER_KEY = "Authorization";
    private final String HEADER_VALUE = "Bearer ";

    private final JsonObject unauthorizedObject;
    private final JsonObject unknownHostErroObject;
    private final JsonObject serverDownObject;
    private final OkHttpClient client;

    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private String email = "";
    private String clientId;

    public static AuthService init() {
        service = new AuthService();
        return service;
    }

    public AuthService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        unauthorizedObject = new JsonObject();
        serverDownObject = new JsonObject();
        unknownHostErroObject = new JsonObject();
    }

    public Task<LoginResponse> loginWithEmailAndPassword(Context context, final String emailIn, final String password) {
        email = emailIn;
        clientId = context.getResources().getString(R.string.is_meniga_androidapp_CLIENT_ID);
        final TaskCompletionSource<LoginResponse> task = new TaskCompletionSource<>(null);
        try {
            JsonObject json = new JsonObject();
            json.addProperty("email", email);
            json.addProperty("password", password);
            json.addProperty("clientId", clientId);
            json.addProperty("clientSecret", BuildConfig.CLIENT_SECRET);

            callToAuthEndpoint(json, BuildConfig.API_BASE_URL + "/authentication").continueWith(new Continuation<JsonObject, Void>() {
                @Override
                public Void then(final Task<JsonObject> resultTask) throws Exception {
                    if (resultTask.isFaulted()) {
                        task.setError(resultTask.getError());
                    } else if (resultTask.isCompleted()) {
                        if (resultTask.getResult() == unauthorizedObject) {
                            task.setResult(LoginResponse.UNAUTHORIZED);
                        } else if (resultTask.getResult() == unknownHostErroObject) {
                            task.setResult(LoginResponse.NO_INTERNET);
                        } else if (resultTask.getResult() == serverDownObject) {
                            task.setResult(LoginResponse.SERVER_MAINTENANCE);
                        } else {
                            task.setResult(LoginResponse.SUCCESS);
                        }
                    } else {
                        task.setResult(LoginResponse.SERVER_ERROR);
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);

        } catch (Exception e) {
            task.setError(e);
        }
        return task.getTask();
    }

    private Task<JsonObject> callToAuthEndpoint(final JsonObject json, final String endpoint) {
        final TaskCompletionSource<JsonObject> task = new TaskCompletionSource<>(null);
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder().url(endpoint).post(body).build();
        synchronized (client) {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call request, @NonNull IOException e) {
                    if (e instanceof UnknownHostException) {
                        task.setResult(unknownHostErroObject);
                    } else {
                        task.setError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == RESPONSE_CODE_UNAUTHORIZED || response.code() == RESPONSE_CODE_BAD_REQUEST) {
                        task.setResult(unauthorizedObject);
                        return;
                    } else if (response.code() == RESPONSE_CODE_SERVER_MAINTENANCE || response.code() == RESPONSE_CODE_SERVER_NOT_FOUND) {
                        task.setResult(serverDownObject);
                        return;
                    }
                    try {
                        String body = response.body().string();
                        JsonElement jsonElement = new JsonParser().parse(body);
                        JsonObject result = jsonElement.getAsJsonObject();
                        result = result.getAsJsonObject("data");
                        parseJWTTokens(result.get("accessToken").getAsString(), result.get("refreshToken").isJsonNull() ? null : result.get("refreshToken").getAsString());
                        task.setResult(result);
                    } catch (Exception e) {
                        task.setError(e);
                    }
                }
            });
        }

        return task.getTask();
    }

    private synchronized Task<LoginResponse> renewToken() {
        final TaskCompletionSource<LoginResponse> task = new TaskCompletionSource<>(null);
        JsonObject json = new JsonObject();
        json.addProperty("refreshToken", refreshToken == null ? "" : refreshToken.getToken());
        json.addProperty("clientId", clientId);
        json.addProperty("clientSecret", BuildConfig.CLIENT_SECRET);
        json.addProperty("subject", email);
        callToAuthEndpoint(json, BuildConfig.API_BASE_URL + "/refresh").continueWith(new Continuation<JsonObject, Void>() {
            @Override
            public Void then(Task<JsonObject> resultTask) throws Exception {
                if (resultTask.isCompleted() &&
                        !resultTask.isFaulted() &&
                        resultTask.getResult() != unauthorizedObject &&
                        resultTask.getResult() != serverDownObject &&
                        resultTask.getResult() != unknownHostErroObject) {
                    refreshToken = new RefreshToken(resultTask.getResult().get("refreshToken").getAsString());
                    task.setResult(LoginResponse.SUCCESS);
                } else if (resultTask.isFaulted() && (resultTask.getError() instanceof SocketTimeoutException || resultTask.getError() instanceof UnknownHostException)) {
                    task.setResult(LoginResponse.TIMEOUT);
                } else if (resultTask.isFaulted()) {
                    task.setError(resultTask.getError());
                } else {
                    if (resultTask.getResult() == unauthorizedObject) {
                        task.setResult(LoginResponse.UNAUTHORIZED);
                    } else if (resultTask.getResult() == serverDownObject) {
                        task.setResult(LoginResponse.SERVER_MAINTENANCE);
                    } else if (resultTask.getResult() == unknownHostErroObject) {
                        task.setResult(LoginResponse.NO_INTERNET);
                    } else {
                        task.setResult(LoginResponse.SERVER_ERROR);
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

        return task.getTask();
    }

    private synchronized void parseJWTTokens(final String at, final String rt) throws Exception {
        if (at != null) {
            accessToken = new AccessToken(at);
        }

        if (rt != null) {
            refreshToken = new RefreshToken(rt);
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private boolean hasAuthHeader(Request request) {
        return request.header(HEADER_KEY) != null;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        synchronized (lock) {
            // Retry 3 times before giving up
            if (responseCount(response) >= 3
                    || refreshToken == null
                    || refreshToken.getToken() == null) {
                return null;
            }

            try {
                if (refreshToken != null && refreshToken.getToken() != null) {
                    Task<LoginResponse> renewResponse = renewToken();
                    renewResponse.waitForCompletion();
                    if (renewResponse.getResult() != LoginResponse.SUCCESS) {
                        return null;
                    }
                }
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + accessToken.getToken())
                    .build();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        if (hasAuthHeader(originalRequest)) {
            return chain.proceed(originalRequest);
        }
        Request authorisedRequest;
        if (accessToken == null) {
            return chain.proceed(originalRequest);
        }
        synchronized (lock) {
            authorisedRequest = originalRequest.newBuilder()
                    .header(HEADER_KEY, HEADER_VALUE + accessToken.getToken())
                    .build();
        }
        return chain.proceed(authorisedRequest);
    }
}
