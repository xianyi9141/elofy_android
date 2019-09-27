package com.peatis.elofy.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.ObjectiveAdapter;
import com.peatis.elofy.model.Objective;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GoalsFragment extends Fragment {

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private OnGoalsListener listener;

    private List<Objective> goals = new ArrayList<>();
    private ObjectiveAdapter adapter;

    public static GoalsFragment newInstance() {
        return new GoalsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_recycler_view, container, false);

        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadGoals(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadGoals(this, true);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && listener != null && adapter == null)
            listener.onLoadGoals(this, true);
    }

    public void handleGoal(List<Objective> goals) {
        this.goals.clear();
        this.goals.addAll(goals);

        adapter = new ObjectiveAdapter(getContext(), this.goals);
        recyclerView.setAdapter(adapter);

        stateView.setViewState(goals.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGoalsListener) {
            listener = (OnGoalsListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnGoalsListener {
        void onLoadGoals(GoalsFragment fragment, boolean activityIndicator);
    }
}
