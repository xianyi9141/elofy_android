package com.peatis.elofy.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.peatis.elofy.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ObjectiveFragment extends Fragment {
    private static final String ARG_PAGE = "page";

    @BindView(R.id.objective_tabbar)
    TabLayout tabLayout;

    @BindView(R.id.objective_viewpager)
    ViewPager viewPager;

    private int page = 0;

    public static ObjectiveFragment newInstance(int page) {
        ObjectiveFragment fragment = new ObjectiveFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE, page);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_objective, container, false);

        ButterKnife.bind(this, view);

        ObjectivePagerAdapter adapter = new ObjectivePagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        Objects.requireNonNull(tabLayout.getTabAt(page)).select();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && (getActivity() instanceof BaseActivity))
            ((BaseActivity) getActivity()).title("Seus Objetivos / Empresa");
    }

    private class ObjectivePagerAdapter extends FragmentPagerAdapter {
        private ObjectivePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return GoalsFragment.newInstance();
                default:
                    return OKRsFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Meus Objetivos";
                default:
                    return "Objetivos da Empresa";
            }
        }
    }
}