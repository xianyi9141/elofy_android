package com.peatis.elofy.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.activities.ObjectiveDetailActivity;
import com.peatis.elofy.model.Objective;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ObjectiveAdapter extends RecyclerView.Adapter<ObjectiveAdapter.ViewHolder> {
    private Context context;
    private List<Objective> dataSource;

    public ObjectiveAdapter(Context context, List<Objective> dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_objective, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_objective, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Objective item = dataSource.get(position);

        if (item.getUser().getImage50px().isEmpty()) {
            holder.avatar.setImageResource(R.drawable.avatar);
        } else {
            Picasso.get().load(Uri.parse(item.getUser().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        }
        holder.title.setText(item.getTitle());
        holder.username.setText(item.getUser().getId() == Elofy.Int(context, Elofy.KEY_USER_ID) ? "Me" : item.getUser().getName());
        holder.timestamp.setText("Até: " + Elofy.dateString(item.getTimestamp(), "yyyy-MM-dd", "dd/MM/yyyy"));
        holder.imgMeta.setImageResource(item.getType().image());
        holder.imgMeta.setColorFilter(Elofy.color(context, item.getType().color()));
        holder.textMeta.setTextColor(Elofy.color(context, item.getType().color()));
        switch (item.getType()) {
            case SHARED:
                holder.textMeta.setText("Compartilhado");
                break;
            case INDIVIDUAL:
                holder.textMeta.setText("Individual");
                break;
            case TEAM:
                holder.textMeta.setText("Grupo");
                break;
            case TEAM_NAME:
                holder.textMeta.setText(item.getTeam().getName());
                break;
        }
        holder.percentage.setProgress((int) (item.getPercentage() * 100));
        holder.percentage.setProgressDrawable(Elofy.drawable(context, item.getColor().progressDrawable()));
        holder.textPercentage.setText((int) item.getPercentage() + "%");
        holder.layout.setOnClickListener(v -> ObjectiveDetailActivity.startActivity(context, item));
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public interface OnItemClickListener {
        void onClick(Objective item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        @BindView(R.id.avatar)
        ImageView avatar;
        @BindView(R.id.textTitle)
        TextView title;
        @BindView(R.id.textUsername)
        TextView username;
        @BindView(R.id.imgMeta)
        ImageView imgMeta;
        @BindView(R.id.textMeta)
        TextView textMeta;
        @BindView(R.id.textTimestamp)
        TextView timestamp;
        @BindView(R.id.percentage)
        ProgressBar percentage;
        @BindView(R.id.textPercentage)
        TextView textPercentage;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }
}
