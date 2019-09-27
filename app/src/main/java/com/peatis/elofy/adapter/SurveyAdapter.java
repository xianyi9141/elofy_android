package com.peatis.elofy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.activities.SurveyDetailActivity;
import com.peatis.elofy.model.Survey;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.ViewHolder> {
    private Context context;
    private List<Survey> dataSource;

    public SurveyAdapter(Context context, List<Survey> dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_survey, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_survey, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Survey item = dataSource.get(position);

        holder.avatar.setImageDrawable(new ColorDrawable(item.isAnswered() ? Color.GRAY : Elofy.color(context, R.color.colorPrimary)));
        holder.username.setText(item.getUserName().length() > 0 ? item.getUserName().toUpperCase().substring(0, 1) : "");
        holder.title.setText(item.getName());
        holder.count.setText(item.getCount());
        holder.timestamp.setText("Atualizado " + Elofy.dateString(item.getTimestamp(), "yyyy-MM-dd", "dd/MM/yyyy"));
        holder.setting.setVisibility(item.isAnswered() ? View.INVISIBLE : View.VISIBLE);
        holder.layout.setOnClickListener(v -> {
            if (!item.isAnswered()) {
                SurveyDetailActivity.startActivity(context, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        @BindView(R.id.avatar)
        CircleImageView avatar;
        @BindView(R.id.textUsername)
        TextView username;
        @BindView(R.id.textTitle)
        TextView title;
        @BindView(R.id.textCount)
        TextView count;
        @BindView(R.id.textTimestamp)
        TextView timestamp;
        @BindView(R.id.btnSetting)
        ImageView setting;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }
}
