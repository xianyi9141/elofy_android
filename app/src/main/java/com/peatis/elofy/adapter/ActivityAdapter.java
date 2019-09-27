package com.peatis.elofy.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.activities.ActivityDetailActivity;
import com.peatis.elofy.model.Activity;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
    private Context context;
    private List<Activity> dataSource;

    public ActivityAdapter(Context context, List<Activity> dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_activity, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity item = dataSource.get(position);

        if (!item.getUser().getImage50px().isEmpty()) {
            Picasso.get().load(Uri.parse(item.getUser().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        } else if (Elofy.string(context, Elofy.KEY_USER_AVATAR_XS) != null && !Elofy.string(context, Elofy.KEY_USER_AVATAR_XS).isEmpty()) {
            Picasso.get().load(Uri.parse(Elofy.string(context, Elofy.KEY_USER_AVATAR_XS))).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.avatar);
        }
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.timestamp.setText("Última atualização: " + Elofy.dateString(item.getEndAt(), "yyyy-MM-dd", "dd/MM/yyyy"));
        holder.imgMeta.setColorFilter(Elofy.color(context, item.status().color()));
        holder.imgMeta.setImageResource(item.status().image());
        holder.textMeta.setTextColor(Elofy.color(context, item.status().color()));
        holder.textMeta.setText(item.status().string());
        holder.layout.setOnClickListener(v -> ActivityDetailActivity.startActivity(context, item));
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
        @BindView(R.id.textDescription)
        TextView description;
        @BindView(R.id.textTimestamp)
        TextView timestamp;
        @BindView(R.id.imgMeta)
        ImageView imgMeta;
        @BindView(R.id.textMeta)
        TextView textMeta;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }
}
