package com.peatis.elofy.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.gson.JsonObject;
import com.kennyc.view.MultiStateView;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.adapter.FilterItemAdapter;
import com.peatis.elofy.adapter.ObjectiveAdapter;
import com.peatis.elofy.model.Filter;
import com.peatis.elofy.model.Objective;
import com.peatis.elofy.model.User;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OKRsFragment extends Fragment {

    @BindView(R.id.searchView)
    SearchView searchView;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.stateView)
    MultiStateView stateView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private OnOKRsListener listener;

    private ObjectiveAdapter adapter;
    private List<Objective> okrs = new ArrayList<>();
    private List<Objective> filteredOKRs = new ArrayList<>();

    private Map<FilterItem, List<Filter>> selection = new HashMap<>();
    private Map<FilterItem, List<Filter>> filterDataSource = new HashMap<>();

    public static OKRsFragment newInstance() {
        return new OKRsFragment();
    }

    @OnClick(R.id.btnFilter)
    void onFilterClicked() {
        if (filterDataSource.keySet().size() != FilterItem.values().length) {
            if (listener != null) listener.onLoadFilter(this);
        } else {
            presentFilterDialog();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_okrs, container, false);

        ButterKnife.bind(this, view);

        // search bar
        searchView.setQueryHint("Digite para pesquisa");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter();
                return true;
            }
        });
        searchView.setOnSearchClickListener(v -> filter());
        searchView.setOnCloseListener(() -> {
            filter();
            return true;
        });
        ((View) searchView.getParent()).requestFocus();

        // recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefresh.setOnRefreshListener(() -> {
            stateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
            if (listener != null) listener.onLoadOKRs(this, false);
        });

        if (getUserVisibleHint() && listener != null) {
            listener.onLoadOKRs(this, true);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && listener != null && adapter == null) listener.onLoadOKRs(this, true);
    }

    public void filter() {
        filteredOKRs.clear();
        filteredOKRs.addAll(okrs);

        if (!selection.isEmpty()) {
            // type
            List<Integer> typeIds = Stream.of(selection.get(FilterItem.TYPE)).map(Filter::getId).toList();
            if (!typeIds.contains(0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> typeIds.contains(okr.getColor().value)).toList();
            }
            // responsible
            List<Integer> responsibleIds = Stream.of(selection.get(FilterItem.RESPONSIBLE)).map(Filter::getId).toList();
            if (!responsibleIds.contains(0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> responsibleIds.contains(okr.getUser().getId())).toList();
            }
            // team
            List<Integer> teamIds = Stream.of(selection.get(FilterItem.TEAM)).map(Filter::getId).toList();
            if (!teamIds.contains(0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> teamIds.contains(okr.getTeam().getId())).toList();
            }
            // year
            List<String> yearIds = Stream.of(selection.get(FilterItem.YEAR)).map(Filter::getName).toList();
            if (Stream.of(selection.get(FilterItem.YEAR)).noneMatch(f -> f.getId() == 0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> yearIds.contains(okr.getYear())).toList();
            }
            // quarter
            List<Integer> quarterIds = Stream.of(selection.get(FilterItem.QUARTER)).map(Filter::getId).toList();
            if (!quarterIds.contains(0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> {
                    List<Integer> ids = Stream.of(okr.getCycles()).map(User::getId).toList();
                    return Stream.of(quarterIds, ids).distinct().count() != (quarterIds.size() + ids.size());
                }).toList();
            }
            // goal
            List<Integer> goalIds = Stream.of(selection.get(FilterItem.GOAL)).map(Filter::getId).toList();
            if (!goalIds.contains(0)) {
                filteredOKRs = Stream.of(filteredOKRs).filter(okr -> goalIds.contains(okr.getParentId())).toList();
            }
        }
        // search text
        String searchText = searchView.getQuery().toString();
        if (!searchText.isEmpty()) {
            filteredOKRs = Stream.of(filteredOKRs).filter(okr -> okr.getRaw().contains(searchText.toLowerCase())).toList();
        }

        adapter = new ObjectiveAdapter(getContext(), filteredOKRs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        stateView.setViewState(filteredOKRs.isEmpty() ? MultiStateView.VIEW_STATE_EMPTY : MultiStateView.VIEW_STATE_CONTENT);
        swipeRefresh.setRefreshing(false);
    }

    public void handleOKRs(List<Objective> okrs) {
        this.okrs.clear();
        this.okrs.addAll(okrs);
        filter();
    }

    public void handleFilters(JsonObject res) {
        // type
        List<Filter> data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.add(new Filter(1, "Em dia"));
        data.add(new Filter(2, "Atenção"));
        data.add(new Filter(3, "Atraso"));
        filterDataSource.put(FilterItem.TYPE, data);

        // responsible
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.addAll(Stream.of(res.get("responsible").getAsJsonArray()).map(elt -> new Filter(elt.getAsJsonObject().get("id").getAsInt(), elt.getAsJsonObject().get("name").getAsString())).toList());
        filterDataSource.put(FilterItem.RESPONSIBLE, data);

        // team
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.addAll(Stream.of(res.get("teams").getAsJsonArray()).map(elt -> new Filter(elt.getAsJsonObject().get("id").getAsInt(), elt.getAsJsonObject().get("name").getAsString())).toList());
        filterDataSource.put(FilterItem.TEAM, data);

        // year
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.addAll(Stream.of(res.get("years").getAsJsonArray()).map(elt -> new Filter(elt.getAsInt(), elt.getAsString())).toList());
        filterDataSource.put(FilterItem.YEAR, data);

        // quarter
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        filterDataSource.put(FilterItem.QUARTER, data);

        // goal
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        filterDataSource.put(FilterItem.GOAL, data);

        presentFilterDialog();
    }

    public void handleFilterYearChanged(JsonObject res, ViewGroup layout) {
        // quarter
        List<Filter> data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.addAll(Stream.of(res.get("quarters").getAsJsonArray()).map(elt -> new Filter(elt.getAsJsonObject().get("id").getAsInt(), elt.getAsJsonObject().get("name").getAsString())).toList());
        filterDataSource.put(FilterItem.QUARTER, data);
        selection.get(FilterItem.QUARTER).clear();
        selection.get(FilterItem.QUARTER).add(new Filter(0, "Todos"));
        setupFilterItem(FilterItem.QUARTER, layout);

        // goal
        data = new ArrayList<>();
        data.add(new Filter(0, "Todos"));
        data.addAll(Stream.of(res.get("goals").getAsJsonArray()).map(elt -> new Filter(elt.getAsJsonObject().get("id").getAsInt(), elt.getAsJsonObject().get("title").getAsString())).toList());
        filterDataSource.put(FilterItem.GOAL, data);
        selection.get(FilterItem.GOAL).clear();
        selection.get(FilterItem.GOAL).add(new Filter(0, "Todos"));
        setupFilterItem(FilterItem.GOAL, layout);
    }

    private void presentFilterDialog() {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_filter, null, false);

        setupFilterItem(FilterItem.TYPE, layout);
        setupFilterItem(FilterItem.RESPONSIBLE, layout);
        setupFilterItem(FilterItem.TEAM, layout);
        setupFilterItem(FilterItem.YEAR, layout);
        setupFilterItem(FilterItem.QUARTER, layout);
        setupFilterItem(FilterItem.GOAL, layout);

        new AlertDialog.Builder(getContext(), R.style.FilterDialogTheme)
                .setTitle("Filtros para pesquisa")
                .setView(layout)
                .setPositiveButton("Aplicar", (dialog, which) -> filter())
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFilterItem(FilterItem item, ViewGroup layout) {
        Context context = getContext();
        TextView itemView = layout.findViewById(item.resId);

        // value
        if (selection.get(item) == null) {
            List<Filter> data = new ArrayList<>();
            data.add(filterDataSource.get(item).get(0));
            selection.put(item, data);
        }
        itemView.setText(StringUtils.join(Stream.of(selection.get(item)).map(Filter::getName).toArray(), ", "));

        // popup
        PopupWindow popup = new PopupWindow(Elofy.pixel(context, 220), ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.filter_bg, context.getTheme()));
        } else {
            popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.filter_bg));
        }
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setTouchInterceptor((v1, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                popup.dismiss();
                return true;
            }
            return false;
        });

        // list
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_list_view, null, false);
        ListView list = linearLayout.findViewById(R.id.listView);
        FilterItemAdapter adapter = new FilterItemAdapter(context, filterDataSource.get(item), selection.get(item));
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            if (item.multiple) {
                selection.get(item).remove(filterDataSource.get(item).get(0));
                Filter data = filterDataSource.get(item).get(position);
                if (selection.get(item).contains(data)) {
                    selection.get(item).remove(data);
                } else {
                    selection.get(item).add(data);
                }
                if (position == 0 || selection.get(item).isEmpty()) {
                    selection.get(item).clear();
                    selection.get(item).add(filterDataSource.get(item).get(0));
                }
                adapter.notifyDataSetChanged();
                itemView.setText(StringUtils.join(Stream.of(selection.get(item)).map(Filter::getName).toArray(), ", "));
            } else {
                selection.get(item).clear();
                selection.get(item).add(filterDataSource.get(item).get(position));
                itemView.setText(filterDataSource.get(item).get(position).getName());
                popup.dismiss();
            }
            if (item == FilterItem.YEAR && listener != null) {
                listener.onFilterYearChanged(this, filterDataSource.get(item).get(position).getId(), layout);
            }
        });

        popup.setContentView(linearLayout);
        itemView.setOnClickListener(v -> {
            if ((item == FilterItem.QUARTER || item == FilterItem.GOAL) && filterDataSource.get(item).size() > 2) {
                popup.showAsDropDown(itemView, 0, -200);
            } else {
                popup.showAsDropDown(itemView);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOKRsListener) {
            listener = (OnOKRsListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    enum FilterItem {
        TYPE(R.id.filterType, false),
        RESPONSIBLE(R.id.filterUser, true),
        TEAM(R.id.filterTeam, true),
        YEAR(R.id.filterYear, false),
        QUARTER(R.id.filterQuarter, false),
        GOAL(R.id.filterGoal, true);

        int resId;
        boolean multiple;

        FilterItem(int resId, boolean multiple) {
            this.resId = resId;
            this.multiple = multiple;
        }
    }

    public interface OnOKRsListener {
        void onLoadOKRs(OKRsFragment fragment, boolean activityIndicator);

        void onLoadFilter(OKRsFragment fragment);

        void onFilterYearChanged(OKRsFragment fragment, int year, ViewGroup layout);
    }
}
