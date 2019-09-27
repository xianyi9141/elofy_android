package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends BaseActivity {
    @BindView(R.id.avatar)
    CircleImageView avatar;

    @BindView(R.id.textHello)
    TextView hello;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.btnGoals)
    void onGoal() {
        MainActivity.startActivity(this, MainActivity.Page.GOALS);
    }

    @OnClick(R.id.btnOKRs)
    void onOKR() {
        MainActivity.startActivity(this, MainActivity.Page.OKRS);
    }

    @OnClick(R.id.btnElos)
    void onElos() {
        MainActivity.startActivity(this, MainActivity.Page.ELOS);
    }

    @OnClick(R.id.btnSurveys)
    void onSurvey() {
        MainActivity.startActivity(this, MainActivity.Page.SURVEYS);
    }

    @OnClick(R.id.btnLogout)
    void onLogout() {
        logout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        String url = Elofy.string(this, Elofy.KEY_USER_AVATAR);
        if (url != null) {
            Picasso.get().load(Uri.parse(url)).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(avatar);
        }
        hello.setText("Olá!, " + Elofy.string(this, Elofy.KEY_USER_NAME) + "!");

        // device token
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            Log.d(TAG, "Firebase token : " + token);
            Map<String, String> params = new HashMap<>();
            params.put("token", token);
            post("/devicetoken", params, false, response -> Log.i(TAG, "New refresh token updated : " + token));
        }

        // if launched by notified clicked
        if (Elofy.Bool(this, Elofy.KEY_NOTIFIED)) {
            Elofy.Bool(this, Elofy.KEY_NOTIFIED, false);
            MainActivity.startActivity(this, MainActivity.Page.ELOS);
        }

    }
}
