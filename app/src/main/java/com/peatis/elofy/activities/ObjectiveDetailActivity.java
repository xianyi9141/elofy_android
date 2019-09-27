package com.peatis.elofy.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.material.tabs.TabLayout;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.KeyResultAdapter;
import com.peatis.elofy.model.KeyResult;
import com.peatis.elofy.model.Objective;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ObjectiveDetailActivity extends BaseActivity {
    static final String ARG_OBJECTIVE = "objective";

    @BindView(R.id.avatar)
    CircleImageView avatar;

    @BindView(R.id.textTitle)
    TextView title;

    @BindView(R.id.textUsername)
    TextView username;

    @BindView(R.id.percentage)
    ProgressBar percentage;

    @BindView(R.id.textPercentage)
    TextView textPercentage;

    @BindView(R.id.tabbar)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    RecyclerView recyclerView;
    TextView description;

    Objective objective;
    KeyResultAdapter adapter;

    BroadcastReceiver activityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            get("/goal/" + objective.getId(), res -> {
                objective = new Objective(res.getAsJsonObject());
                updateUI();
            });
        }
    };

    BroadcastReceiver keyValueAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    public static void startActivity(Context context, Objective objective) {
        Intent intent = new Intent(context, ObjectiveDetailActivity.class);
        intent.putExtra(ARG_OBJECTIVE, objective);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objective_detail);

        showActionBarBack(true);
        title("Detalhes objetivo");
        ButterKnife.bind(this);
        setupViewPager();

        objective = (Objective) getIntent().getSerializableExtra(ARG_OBJECTIVE);
        updateUI();

        // Broadcast receiver
        registerReceiver(activityUpdateReceiver, new IntentFilter(Elofy.BROADCAST_ACTIVITY_UPDATED));
        registerReceiver(keyValueAddedReceiver, new IntentFilter(Elofy.BROADCAST_KEY_VALUE_ADDED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(activityUpdateReceiver);
        unregisterReceiver(keyValueAddedReceiver);
    }

    void updateUI() {
        if (objective.getUser().getImage50px().isEmpty()) {
            avatar.setImageResource(R.drawable.avatar);
        } else {
            Picasso.get().load(Uri.parse(objective.getUser().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(avatar);
        }
        title.setText(objective.getTitle());
        username.setText(objective.getUser().getName() + (objective.getUser().getId() == Elofy.Int(this, Elofy.KEY_USER_ID) ? " (Me)" : ""));
        percentage.setProgress((int) (objective.getPercentage() * 100));
        percentage.setProgressDrawable(Elofy.drawable(this, objective.getColor().progressDrawable()));
        textPercentage.setText((int) objective.getPercentage() + "%");
        description.setText(objective.getDescription());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (objective.getKeyResults() == null) {
            loadResultKeys();
        } else {
            adapter = new KeyResultAdapter(this, objective.getKeyResults(), Elofy.Int(this, Elofy.KEY_USER_ID));
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupViewPager() {
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        description = new TextView(this);
        description.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int px = Elofy.pixel(this, 16);
        description.setPadding(px, px, px, px);
        description.setTextColor(Color.BLACK);
        description.setTextSize(17);

        viewPager.setAdapter(new ObjectiveDetailPageAdapter());
        tabLayout.setupWithViewPager(viewPager);
    }

    private void loadResultKeys() {
        get("/goal/" + objective.getId(), res -> {
            List<KeyResult> keyResults = Stream.of(res.get("keys").getAsJsonArray()).map(key -> new KeyResult(key.getAsJsonObject())).toList();
            objective.setKeyResults(keyResults);
            adapter = new KeyResultAdapter(this, objective.getKeyResults(), Elofy.Int(this, Elofy.KEY_USER_ID));
            recyclerView.setAdapter(adapter);
        });
    }

    private class ObjectiveDetailPageAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object == view;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Key Results" : "Descrição";
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(position == 0 ? recyclerView : description);
            return position == 0 ? recyclerView : description;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
