package com.peatis.elofy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peatis.elofy.R;
import com.peatis.elofy.model.Feed;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private Context context;
    private List<Feed> dataSource;

    public FeedAdapter(Context context, List<Feed> dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_feed, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feed item = dataSource.get(position);

        holder.top.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
        holder.bottom.setVisibility(position < dataSource.size() - 1 ? View.VISIBLE : View.GONE);
        holder.center.setImageResource(item.getType().color());
        holder.timestamp.setText(item.getTimestamp());
        holder.description.setText(item.getText());
        holder.timeDiff.setText(item.getTimeDiff());
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        @BindView(R.id.imgTop)
        ImageView top;
        @BindView(R.id.imgBottom)
        ImageView bottom;
        @BindView(R.id.imgCenter)
        CircleImageView center;
        @BindView(R.id.textTimestamp)
        TextView timestamp;
        @BindView(R.id.textDescription)
        TextView description;
        @BindView(R.id.textTimediff)
        TextView timeDiff;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
        }
    }
}
