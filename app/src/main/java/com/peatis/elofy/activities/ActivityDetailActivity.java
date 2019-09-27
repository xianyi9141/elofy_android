package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.Activity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityDetailActivity extends BaseActivity {
    static final String ARG_ACTIVITY = "activity";

    @BindView(R.id.avatar)
    CircleImageView avatar;

    @BindView(R.id.textTitle)
    TextView title;

    @BindView(R.id.textDescription)
    TextView description;

    @BindView(R.id.textTimestamp)
    TextView timestamp;

    Activity activity;

    public static void startActivity(Context context, Activity activity) {
        Intent intent = new Intent(context, ActivityDetailActivity.class);
        intent.putExtra(ARG_ACTIVITY, activity);
        context.startActivity(intent);
    }

    @OnClick(R.id.btnPending)
    void pending() {
        Map<String, String> params = new HashMap<>();
        params.put("percent", "0");
        params.put("atraso", "0");
        updateStatus(params);
    }

    @OnClick(R.id.btnProgress)
    void progress() {
        Map<String, String> params = new HashMap<>();
        params.put("percent", "50");
        params.put("atraso", "0");
        updateStatus(params);
    }

    @OnClick(R.id.btnDone)
    void done() {
        Map<String, String> params = new HashMap<>();
        params.put("percent", "100");
        params.put("atraso", "0");
        updateStatus(params);
    }

    @OnClick(R.id.btnLate)
    void late() {
        Map<String, String> params = new HashMap<>();
        params.put("percent", "50");
        params.put("atraso", "1");
        updateStatus(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_detail);

        showActionBarBack(true);
        title("Atividades");

        ButterKnife.bind(this);

        activity = (Activity) getIntent().getSerializableExtra(ARG_ACTIVITY);

        if (activity.getUser().getImage50px().isEmpty()) {
            avatar.setImageResource(R.drawable.avatar);
        } else {
            Picasso.get().load(Uri.parse(activity.getUser().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(avatar);
        }
        title.setText(activity.getTitle());
        description.setText(activity.getDescription());
        timestamp.setText("Até: " + Elofy.dateString(activity.getEndAt(), "yyyy-MM-dd", "dd/MM/yyyy"));
    }

    void updateStatus(Map<String, String> params) {
        post("/activity/" + activity.getId(), params, res -> {
            message(res.get("message").getAsString());
            if (res.get("status").getAsInt() == 1) {
                sendBroadcast(new Intent(Elofy.BROADCAST_ACTIVITY_UPDATED));
                finish();
            }
        });
    }
}
