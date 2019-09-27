package com.peatis.elofy.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;

import com.annimon.stream.Stream;
import com.google.android.material.tabs.TabLayout;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.Activity;
import com.peatis.elofy.model.Comment;
import com.peatis.elofy.model.Elos;
import com.peatis.elofy.model.Feed;
import com.peatis.elofy.model.MentionUser;
import com.peatis.elofy.model.Objective;
import com.peatis.elofy.model.Survey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity
        implements GoalsFragment.OnGoalsListener, OKRsFragment.OnOKRsListener,
        ActivitiesFragment.OnActivitiesListener, ElosFragment.OnElosListener,
        FeedsFragment.OnFeedsListener, SurveysFragment.OnSurveysListener {

    static final String ARG_PAGE = "page";
    static final int[] tabIcons = {R.drawable.nav_goal, R.drawable.nav_activity, R.drawable.nav_elos, R.drawable.nav_history, R.drawable.nav_survey};

    @BindView(R.id.tabbar)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    int page = 0;
    Fragment fragment;

    BroadcastReceiver activityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (fragment instanceof ActivitiesFragment) {
                get("/activities", res -> {
                    List<Activity> activities = Stream.of(res.getAsJsonArray("activities")).map(activity -> new Activity(activity.getAsJsonObject())).toList();
                    ((ActivitiesFragment) fragment).handleActivities(activities);
                });
            } else if (fragment instanceof GoalsFragment) {
                getArray("/goals", res -> {
                    List<Objective> goals = Stream.of(res).map(goal -> new Objective(goal.getAsJsonObject())).toList();
                    ((GoalsFragment) fragment).handleGoal(goals);
                });
            } else if (fragment instanceof OKRsFragment) {
                getArray("/okrs", res -> {
                    List<Objective> okrs = Stream.of(res).map(goal -> new Objective(goal.getAsJsonObject())).toList();
                    ((OKRsFragment) fragment).handleOKRs(okrs);
                });
            }
        }
    };

    BroadcastReceiver surveyFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (fragment instanceof SurveysFragment) {
                getArray("/surveys", res -> {
                    List<Survey> surveys = Stream.of(res).map(survey -> new Survey(survey.getAsJsonObject())).toList();
                    ((SurveysFragment) fragment).handleSurveys(surveys);
                });
            }
        }
    };

    public static void startActivity(Context context, Page page) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_PAGE, page.toString());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showActionBarBack(true);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(5);
        tabLayout.setupWithViewPager(viewPager);
        ColorStateList iconColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            iconColor = getResources().getColorStateList(R.color.tab_icon, getTheme());
        } else {
            iconColor = getResources().getColorStateList(R.color.tab_icon);
        }
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setIcon(tabIcons[i]);
            DrawableCompat.setTintList(tab.getIcon(), iconColor);
        }
        Page param = Page.valueOf(getIntent().getStringExtra(ARG_PAGE));
        switch (param) {
            case GOALS:
                title("Seus Objetivos / Empresa");
                page = 0;
                break;

            case OKRS:
                title("Seus Objetivos / Empresa");
                page = 1;
                break;

            case ELOS:
                title("Curtidas");
                tabLayout.getTabAt(2).select();
                break;

            case SURVEYS:
                title("Pesquisas");
                tabLayout.getTabAt(4).select();
                break;

            default:
                break;
        }

        // Broadcast receiver
        registerReceiver(activityUpdateReceiver, new IntentFilter(Elofy.BROADCAST_ACTIVITY_UPDATED));
        registerReceiver(surveyFinishReceiver, new IntentFilter(Elofy.BROADCAST_SURVEY_FINISHED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(activityUpdateReceiver);
        unregisterReceiver(surveyFinishReceiver);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onLoadGoals(GoalsFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        getArray("/goals", activityIndicator, res -> {
            List<Objective> goals = Stream.of(res).map(goal -> new Objective(goal.getAsJsonObject())).toList();
            fragment.handleGoal(goals);
        });
    }

    @Override
    public void onLoadOKRs(OKRsFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        getArray("/okrs", activityIndicator, res -> {
            List<Objective> okrs = Stream.of(res).map(goal -> new Objective(goal.getAsJsonObject())).toList();
            fragment.handleOKRs(okrs);
        });
    }

    @Override
    public void onLoadFilter(OKRsFragment fragment) {
        this.fragment = fragment;
        get("/filters", fragment::handleFilters);
    }

    @Override
    public void onFilterYearChanged(OKRsFragment fragment, int year, ViewGroup layout) {
        this.fragment = fragment;
        get("/filter/" + year, res -> fragment.handleFilterYearChanged(res, layout));
    }

    @Override
    public void onLoadActivities(ActivitiesFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        get("/activities", activityIndicator, res -> {
            List<Activity> activities = Stream.of(res.getAsJsonArray("activities")).map(activity -> new Activity(activity.getAsJsonObject())).toList();
            fragment.handleActivities(activities);
        });
    }

    @Override
    public void onLoadFeeds(FeedsFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        get("/timeline", activityIndicator, res -> {
            if (res.get("status").getAsInt() == 1) {
                List<Feed> feeds = Stream.of(res.getAsJsonArray("feeds")).map(elt -> new Feed(elt.getAsJsonObject())).toList();
                fragment.handleFeeds(feeds);
            } else {
                message(res.get("message").getAsString());
            }
        });
    }

    @Override
    public void onLoadSurveys(SurveysFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        getArray("/surveys", activityIndicator, res -> {
            List<Survey> surveys = Stream.of(res).map(survey -> new Survey(survey.getAsJsonObject())).toList();
            fragment.handleSurveys(surveys);
        });
    }

    @Override
    public void onLoadElos(ElosFragment fragment, boolean activityIndicator) {
        this.fragment = fragment;
        get("/comments", activityIndicator, res -> {
            List<Elos> eloes = Stream.of(res.getAsJsonArray("elos")).map(elt -> new Elos(elt.getAsJsonObject())).toList();
            fragment.handleEloses(eloes);
        });
    }

    @Override
    public void onLoadComments(ElosFragment fragment, Elos elos, int position) {
        this.fragment = fragment;
        get("/comment/" + elos.getId(), res -> {
            List<Comment> comments = Stream.of(res.getAsJsonArray("comments")).map(elt -> new Comment(elt.getAsJsonObject())).toList();
            fragment.handleComments(comments, position);
        });
    }

    @Override
    public void onLikeElos(ElosFragment fragment, Elos elos, int position) {
        this.fragment = fragment;
        get("/likecomment/" + elos.getId(), res -> {
            if (res.get("status").getAsInt() == 1) {
                fragment.handleLikeElos(position);
            } else {
                message(res.get("message").getAsString());
            }
        });
    }

    @Override
    public void onAddComment(ElosFragment fragment, Elos elos, String text, int position) {
        this.fragment = fragment;
        Map<String, String> params = new HashMap<>();
        params.put("comment", text);
        post("/comment/" + elos.getId(), params, res -> {
            if (res.get("status").getAsInt() == 1) {
                Comment comment = new Comment(Elofy.Int(this, Elofy.KEY_USER_ID), Elofy.string(this, Elofy.KEY_USER_NAME), text);
                fragment.handleCommentAdded(comment, position);
            } else {
                message(res.get("message").getAsString());
            }
        });
    }

    @Override
    public void onAddElos(ElosFragment fragment, String targets, String text) {
        this.fragment = fragment;
        Map<String, String> params = new HashMap<>();
        params.put("users", targets);
        params.put("comment", text);
        post("/comments", params, res -> {
            if (res.get("status").getAsInt() == 1) {
                onLoadElos(fragment, true);
            } else {
                message(res.get("message").getAsString());
            }
        });
    }

    @Override
    public void onLoadMentionUsers(ElosFragment fragment) {
        this.fragment = fragment;
        getArray("/mentionusers", res -> {
            List<MentionUser> users = Stream.of(res).map(elt -> new MentionUser(elt.getAsJsonObject())).toList();
            fragment.handleMentionUsers(users);
        });
    }

    enum Page {
        GOALS, OKRS, ACTIVITIES, ELOS, FEEDS, SURVEYS
    }

    public class PageAdapter extends FragmentPagerAdapter {
        PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ObjectiveFragment.newInstance(page);
                case 1:
                    return ActivitiesFragment.newInstance();
                case 2:
                    return ElosFragment.newInstance();
                case 3:
                    return FeedsFragment.newInstance();
                case 4:
                    return SurveysFragment.newInstance();
                default:
                    return ActivitiesFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return tabIcons.length;
        }
    }
}
