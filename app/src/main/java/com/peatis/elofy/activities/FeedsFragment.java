package com.peatis.elofy.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.FeedAdapter;
import com.peatis.elofy.model.Feed;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedsFragment extends Fragment {

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private OnFeedsListener listener;

    private List<Feed> feeds = new ArrayList<>();
    private FeedAdapter adapter;

    public static FeedsFragment newInstance() {
        return new FeedsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recycler_view, container, false);

        ButterKnife.bind(this, view);

        // recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadFeeds(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadFeeds(this, true);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BaseActivity)
                ((BaseActivity) getActivity()).title("Feed");
            if (listener != null && adapter == null) listener.onLoadFeeds(this, true);
        }
    }

    public void handleFeeds(List<Feed> feeds) {
        this.feeds.clear();
        this.feeds.addAll(feeds);

        adapter = new FeedAdapter(getContext(), this.feeds);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
            recyclerView.removeItemDecorationAt(i);
        }
        recyclerView.setAdapter(adapter);

        stateView.setViewState(feeds.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFeedsListener) {
            listener = (OnFeedsListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFeedsListener {
        void onLoadFeeds(FeedsFragment fragment, boolean activityIndicator);
    }
}
