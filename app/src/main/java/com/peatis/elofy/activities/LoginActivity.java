package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.annimon.stream.Stream;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {
    final static String ARG_EMAIL = "email";

    final static int CODE_GOOGLE = 1;
    final static int CODE_OUTLOOK = 2;

    final static String MS_CLIENT_ID = "e9428457-d820-47ed-8d9b-5a05140e5ee2";
    final static String SCOPES[] = {"https://graph.microsoft.com/User.Read"};
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me";

    @NotEmpty(message = "E-mail deve ser preenchida.")
    @Email(message = "E-mail inválido.")
    @BindView(R.id.textEmail)
    TextInputEditText email;

    @NotEmpty(message = "Senha deve ser preenchida.")
    @BindView(R.id.textPassword)
    TextInputEditText password;

    @BindView(R.id.chkStaySignIn)
    CheckBox rememberMe;

    private GoogleSignInClient googleClient;
    private PublicClientApplication msApplication;

    public static void startActivity(Context context, String email) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(ARG_EMAIL, email);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.btnLogin)
    void login() {
        validator.validate();
    }

    @OnClick(R.id.btnResetPassword)
    void resetPassword() {
        ResetPasswordActivity.startActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (Elofy.string(this, Elofy.KEY_DOMAIN) == null) {
//            Elofy.string(this, Elofy.KEY_DOMAIN , "https://app.elofy.com.br");
            Elofy.string(this, Elofy.KEY_DOMAIN, "http://192.168.0.109/elofy/src/trial.elofy.com.br");
        }

        Elofy.Bool(this, Elofy.KEY_NOTIFIED, getIntent().getBooleanExtra(Elofy.KEY_NOTIFIED, false));

        String email = getIntent().getStringExtra(ARG_EMAIL);
        if (email != null) {
            this.email.setText(email);
        }

        if (Elofy.Bool(this, Elofy.KEY_REMEMBER_ME) || Elofy.Bool(this, Elofy.KEY_NOTIFIED)) {
            HomeActivity.startActivity(this);
            return;
        }

        // Configure google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Configure Outlook
        msApplication = new PublicClientApplication(getApplicationContext(), MS_CLIENT_ID);
    }

    @OnClick(R.id.btnDomain)
    void onDomainAction() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_confirm, null);
        final EditText textBox = view.findViewById(R.id.textDomain);

        new AlertDialog.Builder(this)
                .setTitle("Update domain")
                .setMessage("Current domain is \n" + Elofy.string(this, Elofy.KEY_DOMAIN))
                .setPositiveButton("OK", (dialog, which) -> {
                    String domain = textBox.getText().toString();
                    if (domain.endsWith("/")) {
                        domain = domain.substring(0, domain.length() - 1);
                    }
                    if (Patterns.WEB_URL.matcher(domain).matches()) {
                        Elofy.string(this, Elofy.KEY_DOMAIN, domain);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setView(view)
                .create()
                .show();
    }

    @OnClick(R.id.btnGoogle)
    void googleLogin() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            socialLogin(account.getEmail(), CODE_GOOGLE);
        } else {
            Intent intent = googleClient.getSignInIntent();
            startActivityForResult(intent, CODE_GOOGLE);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            socialLogin(account.getEmail(), CODE_GOOGLE);
            message(account.getEmail());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed: " + e.getMessage());
            message("Google sign in failed: " + e.getMessage());
        }
    }

    @OnClick(R.id.btnOutlook)
    void outlookLogin() {
        List<IAccount> accounts;

        try {
            accounts = msApplication.getAccounts();

            if (accounts.size() == 1) {
                msApplication.acquireTokenSilentAsync(SCOPES, accounts.get(0), getAuthSilentCallback());
            } else {
                Stream.of(accounts).forEach(account -> msApplication.removeAccount(account));
                msApplication.acquireToken(this, SCOPES, getAuthInteractiveCallback());
            }
        } catch (Exception e) {
            msApplication.acquireToken(this, SCOPES, getAuthInteractiveCallback());
        }
    }

    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                String token = authenticationResult.getAccessToken();
                Log.d(TAG, "Outlook successfully authenticated: " + token);
                callMSGraph(token);
            }

            @Override
            public void onError(MsalException exception) {
                Log.d(TAG, "Outlook authentication failed: " + exception.getLocalizedMessage());

                if (exception instanceof MsalUiRequiredException) {
                    msApplication.acquireToken(LoginActivity.this, SCOPES, getAuthInteractiveCallback());
                } else {
                    message("Outlook sign in failed: " + exception.getLocalizedMessage());
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
                message("User cancelled login.");
            }
        };
    }

    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                /* Successfully got a token, call graph now */
                String token = authenticationResult.getAccessToken();
                Log.d(TAG, "Successfully authenticated: " + token);
                callMSGraph(token);
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.getLocalizedMessage());
                message("Outlook sign in failed: " + exception.getLocalizedMessage());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
                message("User cancelled login.");
            }
        };
    }

    private void callMSGraph(String token) {
        showActivityIndicator();
        AndroidNetworking.get(MSGRAPH_URL)
                .addHeaders("Authorization", "Bearer " + token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String email = response.isNull("mail") ? response.getString("userPrincipalName") : response.getString("mail");
                            socialLogin(email, CODE_OUTLOOK);
                        } catch (Exception e) {
                            socialLogin(null, CODE_OUTLOOK);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideActivityIndicator();
                        message("Couldn't get graph result: " + anError.getLocalizedMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        } else {
            msApplication.handleInteractiveRequestRedirect(requestCode, resultCode, data);
        }
    }

    private void socialLogin(String email, int loginType) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email == null ? "" : email);
        params.put("loginType", String.valueOf(loginType));

        post("/socialLogin", params, this::processLoggedIn);
    }

    @Override
    public void onValidationSucceeded() {
        Map<String, String> params = new HashMap<>();
        params.put("email", email.getText().toString());
        params.put("password", password.getText().toString());
        post("/login", params, this::processLoggedIn);
    }

    private void processLoggedIn(JsonObject res) {
        if (res.get("status").getAsInt() == 1) {
            Elofy.string(this, Elofy.KEY_AUTH_TOKEN, res.get("token").getAsString());
            Elofy.Int(this, Elofy.KEY_USER_ID, res.get("id").getAsInt());
            Elofy.string(this, Elofy.KEY_USER_EMAIL, res.get("login").getAsString());
            Elofy.string(this, Elofy.KEY_USER_NAME, res.get("nome").getAsString());
            Elofy.string(this, Elofy.KEY_USER_AVATAR, res.get("orignal_image").getAsString());
            Elofy.string(this, Elofy.KEY_USER_AVATAR_XS, res.get("xs_image").getAsString());
            Elofy.string(this, Elofy.KEY_USER_AVATAR_MD, res.get("md_image").getAsString());
            Elofy.Bool(this, Elofy.KEY_REMEMBER_ME, rememberMe.isChecked());
            HomeActivity.startActivity(this);
        } else {
            message(res.get("message").getAsString());
        }
    }
}
