package com.peatis.elofy.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.annimon.stream.Stream;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.OkHttpClient;

import static com.peatis.elofy.Elofy.KEY_AUTH_TOKEN;
import static com.peatis.elofy.Elofy.KEY_DOMAIN;
import static com.peatis.elofy.Elofy.string;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements Validator.ValidationListener {
    public static final String TAG = "com.sitaep.elofy";

    Validator validator;
    View progressView;
    BroadcastReceiver tokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(Elofy.KEY_FIREBASE_TOKEN);
            String authToken = Elofy.string(context, KEY_AUTH_TOKEN);
            if (authToken != null && token != null) {
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                post("/devicetoken", params, false, response -> Log.i(TAG, "New refresh token updated : " + token));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showActionBarBack(false);

        initialize();
    }

    void initialize() {
        // validator
        validator = new Validator(this);
        validator.setValidationListener(this);

        // networking
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);

        // token refresh
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver, new IntentFilter(Elofy.KEY_FIREBASE_TOKEN));
    }

    void showActionBarBack(boolean show) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(show);
            actionBar.setDisplayShowHomeEnabled(show);
        }
    }

    void title(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.actionHome:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void logout() {
        // remove token from server
        if (Elofy.string(BaseActivity.this, KEY_AUTH_TOKEN) != null) {
            Map<String, String> params = new HashMap<>();
            params.put("token", "");
            post("/devicetoken", params, false, response -> Log.i(TAG, "Clear token successful."));
        }

        // clear storage
        String email = Elofy.string(this, Elofy.KEY_USER_EMAIL);
        Elofy.clear(this);

        // navigate to Login Activity
        LoginActivity.startActivity(this, email);
    }

    void dismissKeyboard() {
        View view = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void message(String message) {
        if (message != null)
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    void showActivityIndicator() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (progressView == null) {
            progressView = getLayoutInflater().inflate(R.layout.progress_bar_overlay, null);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            viewGroup.addView(progressView);
        }
    }

    void hideActivityIndicator() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (progressView != null) {
            ViewGroup viewGroup = findViewById(android.R.id.content);
            viewGroup.removeView(progressView);
            progressView = null;
        }
    }

    protected void post(String url, JsonObjectListener listener) {
        post(url, null, true, listener);
    }

    protected void post(String url, Map<String, String> params, JsonObjectListener listener) {
        post(url, params, true, listener);
    }

    protected void post(String url, boolean showActivityIndicator, JsonObjectListener listener) {
        post(url, null, showActivityIndicator, listener);
    }

    protected void post(String url, Map<String, String> params, boolean showActivityIndicator, JsonObjectListener listener) {
        if (showActivityIndicator) showActivityIndicator();
        ANRequest.PostRequestBuilder builder = AndroidNetworking.post(string(this, KEY_DOMAIN) + "/api" + url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Accept", "application/json");

        if (Elofy.string(this, KEY_AUTH_TOKEN) != null)
            builder.addHeaders("Authorization", "Bearer " + Elofy.string(this, KEY_AUTH_TOKEN));

        if (params != null)
            builder.addBodyParameter(params);

        builder.build().getAsJSONObject(handleResponse(listener));
    }

    protected void get(String url, JsonObjectListener listener) {
        get(url, null, true, listener);
    }

    protected void get(String url, Map<String, String> params, JsonObjectListener listener) {
        get(url, params, true, listener);
    }

    protected void get(String url, boolean showActivityIndicator, JsonObjectListener listener) {
        get(url, null, showActivityIndicator, listener);
    }

    protected void get(String url, Map<String, String> params, boolean showActivityIndicator, JsonObjectListener listener) {
        if (showActivityIndicator) showActivityIndicator();
        ANRequest.GetRequestBuilder builder = AndroidNetworking.get(string(this, KEY_DOMAIN) + "/api" + url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Accept", "application/json");

        if (Elofy.string(this, KEY_AUTH_TOKEN) != null)
            builder.addHeaders("Authorization", "Bearer " + Elofy.string(this, KEY_AUTH_TOKEN));

        if (params != null)
            builder.addQueryParameter(params);

        builder.build().getAsJSONObject(handleResponse(listener));
    }

    protected void getArray(String url, JsonArrayListener listener) {
        getArray(url, null, true, listener);
    }

    protected void getArray(String url, Map<String, String> params, JsonArrayListener listener) {
        getArray(url, params, true, listener);
    }

    protected void getArray(String url, boolean showActivityIndicator, JsonArrayListener listener) {
        getArray(url, null, showActivityIndicator, listener);
    }

    protected void getArray(String url, Map<String, String> params, boolean showActivityIndicator, JsonArrayListener listener) {
        if (showActivityIndicator) showActivityIndicator();
        ANRequest.GetRequestBuilder builder = AndroidNetworking.get(string(this, KEY_DOMAIN) + "/api" + url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Accept", "application/json");

        if (Elofy.string(this, KEY_AUTH_TOKEN) != null)
            builder.addHeaders("Authorization", "Bearer " + Elofy.string(this, KEY_AUTH_TOKEN));

        if (params != null)
            builder.addQueryParameter(params);

        builder.build().getAsJSONArray(handleResponse(listener));
    }

    protected void multipart(String url, Map<String, File> files, Map<String, String> params, JsonObjectListener listener) {
        showActivityIndicator();
        ANRequest.MultiPartBuilder builder = AndroidNetworking.upload(string(this, KEY_DOMAIN) + "/api" + url)
                .addMultipartFile(files)
                .addMultipartParameter(params)
                .addHeaders("Content-Type", "multipart/form-data")
                .addHeaders("Accept", "application/json");

        if (Elofy.string(this, KEY_AUTH_TOKEN) != null)
            builder.addHeaders("Authorization", "Bearer " + Elofy.string(this, KEY_AUTH_TOKEN));

        builder.build().getAsJSONObject(handleResponse(listener));
    }

    protected JSONObjectRequestListener handleResponse(JsonObjectListener listener) {
        return new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                hideActivityIndicator();
                listener.onResponse(new JsonParser().parse(response.toString()).getAsJsonObject());
            }

            @Override
            public void onError(ANError anError) {
                hideActivityIndicator();
                Log.d(TAG, anError.getErrorDetail());
                switch (anError.getErrorCode()) {
                    case 401: // Authentication failed
                        logout();
                        break;

                    case 400: // Validation failed
                        String message;

                        JsonObject json = new JsonParser().parse(anError.getErrorBody()).getAsJsonObject();

                        if (json.has("error")) {
                            JsonObject errorJson = new JsonParser().parse(anError.getErrorBody()).getAsJsonObject().get("errors").getAsJsonObject();
                            Set<String> errorKeys = errorJson.keySet();
                            List<String> errorList = new ArrayList<>();
                            Stream.of(errorKeys).forEach(key -> errorList.addAll(Stream.of(errorJson.get(key).getAsJsonArray()).map(JsonElement::getAsString).toList()));
                            StringBuilder builder = new StringBuilder();
                            for (String msg : errorList) {
                                builder.append(msg);
                                builder.append("\n");
                            }
                            message = builder.substring(0, builder.length() - 1);
                        } else if (json.has("message")) {
                            message = json.get("message").getAsString();
                        } else {
                            message = "Invalid request";
                        }

                        message(message);
                        break;

                    case 404:
                        Log.d(TAG, "Page not found: 404");

                    default:
                        message("Algo deu errado, tente novamente.");
                }
            }
        };
    }

    protected JSONArrayRequestListener handleResponse(JsonArrayListener listener) {
        return new JSONArrayRequestListener() {
            @Override
            public void onResponse(JSONArray response) {
                hideActivityIndicator();
                listener.onResponse(new JsonParser().parse(response.toString()).getAsJsonArray());
            }

            @Override
            public void onError(ANError anError) {
                hideActivityIndicator();
                Log.d(TAG, anError.getErrorDetail());
                switch (anError.getErrorCode()) {
                    case 401: // Authentication failed
                        if (Elofy.string(BaseActivity.this, KEY_AUTH_TOKEN) != null) {
                            logout();
                        }
                        break;

                    case 400: // Validation failed
                        JsonObject errorJson = new JsonParser().parse(anError.getErrorBody()).getAsJsonObject().get("errors").getAsJsonObject();
                        Set<String> errorKeys = errorJson.keySet();
                        List<String> errorList = new ArrayList<>();
                        Stream.of(errorKeys).forEach(key -> errorList.addAll(Stream.of(errorJson.get(key).getAsJsonArray()).map(JsonElement::getAsString).toList()));
                        StringBuilder builder = new StringBuilder();
                        for (String message : errorList) {
                            builder.append(message);
                            builder.append("\n");
                        }
                        String message = builder.substring(0, builder.length() - 1);
                        message(message);
                        break;

                    case 404:
                        Log.d(TAG, "Invalid page, make suer url is correct: 404");

                    default:
                        message("Algo deu errado, tente novamente.");
                }
            }
        };
    }

    @Override
    public void onValidationSucceeded() {

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            String message = error.getCollatedErrorMessage(this);
            message(message);
        }
    }

    interface JsonObjectListener {
        void onResponse(JsonObject response);
    }

    interface JsonArrayListener {
        void onResponse(JsonArray response);
    }
}
