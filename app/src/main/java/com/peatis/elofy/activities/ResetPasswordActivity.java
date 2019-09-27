package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.peatis.elofy.R;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetPasswordActivity extends BaseActivity {

    @NotEmpty(message = "E-mail deve ser preenchida.")
    @Email(message = "E-mail inválido.")
    @BindView(R.id.textEmail)
    TextInputEditText email;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ResetPasswordActivity.class));
    }

    @OnClick(R.id.btnSend)
    void onSendAction() {
        validator.validate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        showActionBarBack(true);
        title("Altere a senha");
        ButterKnife.bind(this);
    }

    @Override
    public void onValidationSucceeded() {
        Map<String, String> params = new HashMap<>();
        params.put("email", email.getText().toString());
        post("/resetpassword", params, res -> {
            if (res.get("status").getAsInt() == 1) {
                new AlertDialog.Builder(this)
                        .setTitle("Email enviado!")
                        .setMessage("Nós enviamos um email para " + email.getText() + " com as as instruções e link para alterar a senha.")
                        .setPositiveButton("Okay", null)
                        .create()
                        .show();
            } else {
                message(res.get("message").getAsString());
            }
        });
    }
}
