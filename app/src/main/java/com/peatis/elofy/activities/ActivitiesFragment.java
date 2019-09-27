package com.peatis.elofy.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.ActivityAdapter;
import com.peatis.elofy.model.Activity;

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

public class ActivitiesFragment extends Fragment {

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private OnActivitiesListener listener;

    List<Activity> activities = new ArrayList<>();
    private ActivityAdapter adapter;

    public static ActivitiesFragment newInstance() {
        return new ActivitiesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recycler_view, container, false);

        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadActivities(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadActivities(this, true);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BaseActivity)
                ((BaseActivity) getActivity()).title("Atividades");
            if (listener != null && adapter == null) listener.onLoadActivities(this, true);
        }
    }

    public void handleActivities(List<Activity> activities) {
        this.activities.clear();
        this.activities.addAll(activities);

        adapter = new ActivityAdapter(getContext(), this.activities);
        recyclerView.setAdapter(adapter);

        stateView.setViewState(activities.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnActivitiesListener) {
            listener = (OnActivitiesListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnActivitiesListener {
        void onLoadActivities(ActivitiesFragment fragment, boolean activityIndicator);
    }
}
