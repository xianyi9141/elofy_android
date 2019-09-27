package com.peatis.elofy.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.kennyc.view.MultiStateView;
import com.linkedin.android.spyglass.suggestions.SuggestionsResult;
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager;
import com.linkedin.android.spyglass.tokenization.QueryToken;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig;
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.ElosAdapter;
import com.peatis.elofy.model.Comment;
import com.peatis.elofy.model.Elos;
import com.peatis.elofy.model.MentionUser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

public class ElosFragment extends Fragment implements ElosAdapter.OnElosAdapterListener,
        SuggestionsResultListener, QueryTokenReceiver, SuggestionsVisibilityManager {
    private View rootView;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.form)
    ConstraintLayout form;

    @BindView(R.id.textBox)
    MentionsEditText textBox;

    @BindView(R.id.btnSend)
    ImageView btnSend;

    @BindView(R.id.suggestionView)
    RecyclerView suggestionsView;

    private OnElosListener listener;

    private List<MentionUser> mentionUsers;
    private List<Elos> eloses = new ArrayList<>();
    private ElosAdapter adapter;

    public static ElosFragment newInstance() {
        return new ElosFragment();
    }

    @OnFocusChange(R.id.textBox)
    void onTextBoxFocusChanged() {
        if (textBox.hasFocus()) {
            if (mentionUsers == null) {
                if (listener != null) listener.onLoadMentionUsers(this);
            } else {
                textBox.setQueryTokenReceiver(this);
                textBox.setSuggestionsVisibilityManager(this);
            }
        }
    }

    @OnClick(R.id.btnSend)
    void onAddElosAction() {
        if (textBox.getText().toString().isEmpty()) {
            return;
        }
        if (textBox.getMentionsText().getMentionSpans().isEmpty()) {
            Toast.makeText(Objects.requireNonNull(getContext()).getApplicationContext(), "Por favor informe @ e o nome do usuário.", Toast.LENGTH_LONG).show();
            return;
        }

        List<MentionUser> mentions = Stream.of(textBox.getMentionsText().getMentionSpans()).map(m -> (MentionUser) m.getMention()).toList();
        if (listener != null)
            listener.onAddElos(this, StringUtils.join(Stream.of(mentions).map(MentionUser::getId).toArray(), ","), textBox.getText().toString());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_elos, container, false);

        ButterKnife.bind(this, view);

        suggestionsView.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionsView.setAdapter(new MentionUserAdapter(new ArrayList<>()));
        textBox.setTokenizer(new WordTokenizer(new WordTokenizerConfig.Builder().setThreshold(0).build()));
        btnSend.setColorFilter(Elofy.color(getContext(), R.color.colorPrimary));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadElos(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadElos(this, true);
        }

        rootView = view;
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BaseActivity)
                ((BaseActivity) getActivity()).title("Curtidas");
            if (listener != null && adapter == null) listener.onLoadElos(this, true);
        }
    }

    public void handleEloses(List<Elos> eloses) {
        this.eloses.clear();
        this.eloses.addAll(eloses);

        adapter = new ElosAdapter(getContext(), this.eloses, this);
        recyclerView.setAdapter(adapter);

        stateView.setViewState(eloses.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);

        textBox.setText("");
        rootView.requestFocus();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).dismissKeyboard();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnElosListener) {
            listener = (OnElosListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onLoadComments(int position) {
        if (listener != null) listener.onLoadComments(this, eloses.get(position), position);
    }

    public void handleComments(List<Comment> comments, int position) {
        eloses.get(position).setComments(comments);
        eloses.get(position).setExpanded(true);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onLikeElos(int position) {
        if (listener != null) listener.onLikeElos(this, eloses.get(position), position);
    }

    public void handleLikeElos(int position) {
        eloses.get(position).setLiked(true);
        eloses.get(position).increaseLikeCnt();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onAddComment(String text, int position) {
        if (listener != null) listener.onAddComment(this, eloses.get(position), text, position);
    }

    public void handleCommentAdded(Comment comment, int position) {
        if (eloses.get(position).getComments() != null) {
            eloses.get(position).getComments().add(comment);
        }
        eloses.get(position).increaseCommentCnt();
        adapter.notifyItemChanged(position);
        rootView.requestFocus();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).dismissKeyboard();
        }
    }

    public void handleMentionUsers(List<MentionUser> users) {
        mentionUsers = users;
        suggestionsView.swapAdapter(new MentionUserAdapter(mentionUsers), true);
        textBox.setQueryTokenReceiver(this);
        textBox.setSuggestionsVisibilityManager(this);
    }

    @Override
    public List<String> onQueryReceived(@NonNull QueryToken queryToken) {
        String query = queryToken.getKeywords().toLowerCase();
        List<MentionUser> matched = query.isEmpty() ? mentionUsers : Stream.of(mentionUsers).filter(user -> user.getName().toLowerCase().contains(query)).toList();
        SuggestionsResult result = new SuggestionsResult(queryToken, matched);
        onReceiveSuggestionsResult(result, "User");
        return Arrays.asList("User");
    }

    @Override
    public void onReceiveSuggestionsResult(@NonNull SuggestionsResult result, @NonNull String bucket) {
        List<? extends Suggestible> suggestions = result.getSuggestions();
        suggestionsView.swapAdapter(new MentionUserAdapter(result.getSuggestions()), true);
        boolean display = suggestions != null && suggestions.size() > 0;
        displaySuggestions(display);
    }

    @Override
    public void displaySuggestions(boolean display) {
        suggestionsView.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isDisplayingSuggestions() {
        return false;
    }

    public interface OnElosListener {
        void onLoadElos(ElosFragment fragment, boolean activityIndicator);

        void onLoadComments(ElosFragment fragment, Elos elos, int position);

        void onLikeElos(ElosFragment fragment, Elos elos, int position);

        void onAddComment(ElosFragment fragment, Elos elos, String text, int position);

        void onAddElos(ElosFragment fragment, String targets, String text);

        void onLoadMentionUsers(ElosFragment fragment);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view;
        }
    }

    private class MentionUserAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<? extends Suggestible> suggestions;

        public MentionUserAdapter(List<? extends Suggestible> users) {
            suggestions = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Suggestible suggestion = suggestions.get(i);
            if (!(suggestion instanceof MentionUser)) {
                return;
            }

            final MentionUser user = (MentionUser) suggestion;
            viewHolder.name.setText(user.getName());
            viewHolder.itemView.setOnClickListener(v -> {
                textBox.insertMention(user);
                suggestionsView.swapAdapter(new MentionUserAdapter(new ArrayList<MentionUser>()), true);
                displaySuggestions(false);
                textBox.requestFocus();
            });
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }
    }
}
