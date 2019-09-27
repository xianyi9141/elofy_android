package com.peatis.elofy.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.SurveyAdapter;
import com.peatis.elofy.model.Survey;

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

public class SurveysFragment extends Fragment {

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private OnSurveysListener listener;

    private List<Survey> surveys = new ArrayList<>();
    private SurveyAdapter adapter;

    public static SurveysFragment newInstance() {
        return new SurveysFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recycler_view, container, false);

        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadSurveys(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadSurveys(this, true);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BaseActivity)
                ((BaseActivity) getActivity()).title("Pesquisas");
            if (listener != null && adapter == null) listener.onLoadSurveys(this, true);
        }
    }

    public void handleSurveys(List<Survey> surveys) {
        this.surveys.clear();
        this.surveys.addAll(surveys);

        adapter = new SurveyAdapter(getContext(), this.surveys);
        recyclerView.setAdapter(adapter);

        stateView.setViewState(surveys.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSurveysListener) {
            listener = (OnSurveysListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnSurveysListener {
        void onLoadSurveys(SurveysFragment fragment, boolean activityIndicator);
    }
}
