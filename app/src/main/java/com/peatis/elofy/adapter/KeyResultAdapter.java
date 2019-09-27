package com.peatis.elofy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.activities.ActivityDetailActivity;
import com.peatis.elofy.activities.KeyResultActivity;
import com.peatis.elofy.model.Activity;
import com.peatis.elofy.model.KeyResult;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.TransitionManager;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class KeyResultAdapter extends RecyclerView.Adapter<KeyResultAdapter.ViewHolder> {
    private Context context;
    private List<KeyResult> dataSource;
    private int userId;

    public KeyResultAdapter(Context context, List<KeyResult> dataSource, int userId) {
        this.context = context;
        this.dataSource = dataSource;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_key_result, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_key_result, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KeyResult item = dataSource.get(position);

        if (item.getUser().getImage50px().isEmpty()) {
            holder.avatar.setImageResource(R.drawable.avatar);
        } else {
            Picasso.get().load(Uri.parse(item.getUser().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        }
        holder.title.setText(item.getTitle());
        holder.username.setText(item.getUser().getName());
        holder.percentage.setProgress((int) (item.getPercentage() * 100));
        holder.textPercentage.setText((int) item.getPercentage() + "%");
        if (holder.activitiesContainer.getChildCount() > 1) {
            holder.activitiesContainer.removeViews(1, holder.activitiesContainer.getChildCount() - 1);
        }
        for (Activity activity : item.getActivities()) {
            holder.activitiesContainer.addView(getActivityView(activity), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        holder.activitiesContainer.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        holder.setting.setVisibility(userId == item.getUser().getId() ? View.VISIBLE : View.INVISIBLE);
        holder.setting.setOnClickListener(v -> KeyResultActivity.startActivity(context, item));
        holder.layout.setOnClickListener(v -> {
            if (item.getActivities().isEmpty()) return;

            TransitionManager.beginDelayedTransition((ViewGroup) holder.layout.getParent());
            if (item.isExpanded()) {
                item.setExpanded(false);
                holder.activitiesContainer.setVisibility(View.GONE);
            } else {
                item.setExpanded(true);
                holder.activitiesContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private LinearLayout getActivityView(Activity activity) {
        Spannable spannable = new SpannableString(activity.getTitle() + "\n" + activity.getUser().getName() + "\n" + activity.getStartAt() + " - " + activity.getEndAt());
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                0,
                activity.getTitle().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#10BBF7")),
                activity.getTitle().length(),
                activity.getTitle().length() + activity.getUser().getName().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        TextView content = new TextView(context);
        content.setText(spannable, TextView.BufferType.SPANNABLE);

        TextView percent = new TextView(context);
        percent.setText((int) activity.getPercentage() + "%");
        percent.setTextSize(12);
        percent.setGravity(Gravity.END);
        percent.setTextColor(Color.parseColor("#888888"));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.TOP);
        layout.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(percent, new LinearLayout.LayoutParams(Elofy.pixel(context, 40), ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        layout.setOnClickListener(v -> ActivityDetailActivity.startActivity(context, activity));

        return layout;
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
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
        @BindView(R.id.activitiesContainer)
        LinearLayout activitiesContainer;
        @BindView(R.id.btnSetting)
        ImageView setting;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }
}
