package com.peatis.elofy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.Comment;
import com.peatis.elofy.model.Elos;
import com.peatis.elofy.model.User;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.TransitionManager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ElosAdapter extends RecyclerView.Adapter<ElosAdapter.ViewHolder> {
    private OnElosAdapterListener listener;

    private Context context;
    private List<Elos> dataSource;

    public ElosAdapter(Context context, List<Elos> dataSource, OnElosAdapterListener listener) {
        this.context = context;
        this.dataSource = dataSource;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context instanceof AppCompatActivity) {
            View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.item_elos, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_elos, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Elos item = dataSource.get(position);

        // avatar image
        if (item.getSender().getImage50px().isEmpty()) {
            holder.avatar.setImageResource(R.drawable.avatar);
        } else {
            Picasso.get().load(Uri.parse(item.getSender().getImage50px())).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        }

        // From To > "Sender to Receiver1, Receiver2, ..."
        String users = item.getSender().getName() + " to " + StringUtils.join(Stream.of(item.getReceiver()).map(User::getName).toArray(), ", ");
        Spannable spannable = new SpannableString(users);
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                0,
                item.getSender().getName().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                (item.getSender().getName() + " to ").length(),
                users.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        holder.fromTo.setText(spannable, TextView.BufferType.SPANNABLE);

        // timestamp
        holder.timestamp.setText(item.getTimeDiff());

        // content
        holder.text.setText(item.getDescription());

        // add comment
        holder.textBox.setText("");
        holder.btnSend.setOnClickListener(v -> {
            if (!holder.textBox.getText().toString().isEmpty() && listener != null) {
                listener.onAddComment(holder.textBox.getText().toString(), position);
            }
        });

        // comments
        if (item.getComments() != null) {
            holder.container.removeAllViews();
            Stream.of(item.getComments()).forEach(comment -> {
                holder.container.addView(getCommentView(comment), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            });
        }

        // comment button
        holder.textComment.setText(item.getCommentCnt() + " Comentários");
        holder.imgComment.setVisibility(item.getCommentCnt() == 0 ? View.GONE : View.VISIBLE);
        expandComment(holder, item);
        holder.btnComment.setOnClickListener(v -> {
            if (item.getCommentCnt() == 0) {
                return;
            }
            if (item.getComments() == null) {
                if (listener != null) listener.onLoadComments(position);
            } else {
                item.setExpanded(!item.isExpanded());
                expandComment(holder, item);
            }
        });

        // like button
        holder.textLike.setText(item.getLikeCnt() + " Curtir");
        holder.imgLike.setImageResource(item.isLiked() ? R.drawable.but_liked : R.drawable.but_like);
        holder.btnLike.setOnClickListener(v -> {
            if (item.isLiked()) {
                return;
            }
            if (listener != null) listener.onLikeElos(position);
        });
    }

    private TextView getCommentView(Comment comment) {
        Spannable spannable = new SpannableString(comment.getUsername() + " " + comment.getText());
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                0,
                comment.getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        TextView content = new TextView(context);
        content.setText(spannable, TextView.BufferType.SPANNABLE);
        content.setTextColor(Color.BLACK);

        return content;
    }

    void expandComment(ViewHolder holder, Elos item) {
        if (item.isExpanded()) {
            if (holder.layout.getParent() != null)
                TransitionManager.beginDelayedTransition((ViewGroup) holder.layout.getParent());
            holder.container.setVisibility(View.VISIBLE);
            holder.imgComment.setImageResource(R.drawable.arrow_up);
        } else {
            if (holder.layout.getParent() != null)
                TransitionManager.beginDelayedTransition((ViewGroup) holder.layout.getParent());
            holder.container.setVisibility(View.GONE);
            holder.imgComment.setImageResource(R.drawable.arrow_down);
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public interface OnElosAdapterListener {
        void onLoadComments(int position);

        void onLikeElos(int position);

        void onAddComment(String text, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        @BindView(R.id.avatar)
        CircleImageView avatar;
        @BindView(R.id.textFromTo)
        TextView fromTo;
        @BindView(R.id.textTimestamp)
        TextView timestamp;
        @BindView(R.id.textDescription)
        TextView text;
        @BindView(R.id.form)
        EditText textBox;
        @BindView(R.id.btnSend)
        ImageView btnSend;
        @BindView(R.id.commentContainer)
        LinearLayout container;
        @BindView(R.id.btnComment)
        LinearLayout btnComment;
        @BindView(R.id.textBtnComment)
        TextView textComment;
        @BindView(R.id.imgBtnComment)
        ImageView imgComment;
        @BindView(R.id.btnLike)
        LinearLayout btnLike;
        @BindView(R.id.textBtnLike)
        TextView textLike;
        @BindView(R.id.imgBtnLike)
        ImageView imgLike;

        ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);

            btnSend.setColorFilter(Elofy.color(context, R.color.colorPrimary));
        }
    }
}
