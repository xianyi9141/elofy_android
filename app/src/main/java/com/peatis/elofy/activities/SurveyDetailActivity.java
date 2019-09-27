package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.Question;
import com.peatis.elofy.model.Survey;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class SurveyDetailActivity extends BaseActivity {
    static final String ARG_SURVEY = "survey";

    @BindView(R.id.questionsContainer)
    LinearLayout container;

    @BindView(R.id.avatar)
    CircleImageView avatar;

    @BindView(R.id.textUsername)
    TextView username;

    @BindView(R.id.textTitle)
    TextView title;

    @BindView(R.id.btnFinishSurvey)
    Button finish;
    Survey survey;
    List<Question> questions = new ArrayList<>();
    Map<String, Map<String, String>> answerStar = new HashMap<>();
    Map<String, Map<String, String>> answerHeart = new HashMap<>();
    Map<String, EditText> answerText = new HashMap<>();
    Map<String, String> answerMultiple = new HashMap<>();
    int padding = 9;
    String[] agrees = {"Discordo totalmente", "Discordo", "Neutro", "Concordo", "Totalmente de acordo"};

    public static void startActivity(Context context, Survey survey) {
        Intent intent = new Intent(context, SurveyDetailActivity.class);
        intent.putExtra(ARG_SURVEY, survey);
        context.startActivity(intent);
    }

    @OnClick(R.id.btnFinishSurvey)
    void finishSurvey() {
        Map<String, Object> answers = new HashMap<>();
        // star
        answers.put("rating_answer", answerStar);
        // heart
        answers.put("heart_answer", answerHeart);
        // text
        Map<String, Map<String, String>> texts = new HashMap<>();
        Stream.of(answerText).forEach(entry -> {
            Map<String, String> item = new HashMap<>();
            item.put("id_questionario", survey.getQuestionId());
            item.put("value", entry.getValue().getText().toString());
            texts.put(entry.getKey(), item);
        });
        answers.put("text_answer", texts);
        // multiple
        answers.put("multiresponse_answer", answerMultiple);

        Map<String, String> params = new HashMap<>();
        params.put("id_pesquisa", survey.getId());
        params.put("questioerio_id", survey.getQuestionId());
        params.put("answers", (new Gson()).toJson(answers));

        post("/survey", params, res -> {
            sendBroadcast(new Intent(Elofy.BROADCAST_SURVEY_FINISHED));
            new AlertDialog.Builder(this)
                    .setTitle("Parabéns!")
                    .setMessage("Pesquisa respondida com sucesso.")
                    .setPositiveButton("Vá para página de pesquisas", (dialog, which) -> {
                        finish();
                    })
                    .create()
                    .show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        showActionBarBack(true);
        title("Pesquisas");

        ButterKnife.bind(this);

        // params
        survey = (Survey) getIntent().getSerializableExtra(ARG_SURVEY);

        padding = Elofy.pixel(this, 4);

        // load UI
        avatar.setImageDrawable(new ColorDrawable(survey.isAnswered() ? Color.GRAY : Elofy.color(this, R.color.colorPrimary)));
        username.setText(survey.getUserName().length() > 0 ? survey.getUserName().toUpperCase().substring(0, 1) : "");
        title.setText(survey.getName());

        loadQuestions();
    }

    void loadQuestions() {
        Map<String, String> params = new HashMap<>();
        params.put("question", survey.getQuestionId());
        getArray("/survey/" + survey.getId(), params, res -> {
            questions = Stream.of(res).map(s -> new Question(s.getAsJsonObject())).toList();
            Stream.of(questions).forEach(question -> {
                container.addView(questionView(question), container.getChildCount() - 1, layoutParams(0, 0));
            });
            finish.setVisibility(View.VISIBLE);
        });
    }

    View questionView(Question question) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // label
        TextView label = new TextView(this);
        label.setText(question.getQuestion());
        label.setTextColor(Color.BLACK);
        label.setTextSize(17);

        layout.addView(label, layoutParams(0, 0));

        switch (question.getType()) {
            case STAR:
                BaseRatingBar star = new BaseRatingBar(this);
                star.setEmptyDrawableRes(R.drawable.star);
                star.setFilledDrawableRes(R.drawable.star_d);
                star.setNumStars(5);
                star.setRating(0);
                star.setStepSize(1);
                star.setStarWidth(starSize());
                star.setStarHeight(starSize());
                star.setStarPadding(padding);
                star.setOnRatingChangeListener((baseRatingBar, v) -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("id_questionario", survey.getQuestionId());
                    if (baseRatingBar.getRating() > 0) {
                        item.put("value", ((int) baseRatingBar.getRating()) + "");
                    }
                    answerStar.put(question.getQueryId(), item);
                });
                layout.addView(star, layoutParams(0, 0));
                break;

            case HEART:
                BaseRatingBar heart = new BaseRatingBar(this);
                heart.setEmptyDrawableRes(R.drawable.heart);
                heart.setFilledDrawableRes(R.drawable.heart_d);
                heart.setNumStars(10);
                heart.setRating(0);
                heart.setStepSize(1);
                heart.setStarWidth(starSize());
                heart.setStarHeight(starSize());
                heart.setStarPadding(padding);
                heart.setOnRatingChangeListener((baseRatingBar, v) -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("id_questionario", survey.getQuestionId());
                    if (baseRatingBar.getRating() > 0) {
                        item.put("value", ((int) baseRatingBar.getRating()) + "");
                    }
                    answerHeart.put(question.getQueryId(), item);
                });
                layout.addView(heart, layoutParams(0, 0));
                break;

            case TEXT:
                EditText text = new EditText(this);
                text.setHint("Descreva sua resposta aqui");
                text.setPadding(padding * 2, padding * 2, padding * 2, padding * 2);
                text.setBackgroundResource(R.drawable.round_grey_border);
                text.setGravity(Gravity.TOP);
                text.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                layout.addView(text, layoutParams(0, Elofy.pixel(this, 120)));
                layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                layout.setDividerDrawable(Elofy.drawable(this, R.drawable.spacer));
                answerText.put(question.getQueryId(), text);
                break;

            case MULTIPLE:
                RadioGroup group = new RadioGroup(this);
                Stream.of(question.getOptions()).forEach(option -> {
                    RadioButton radio = new RadioButton(this);
                    radio.setText(option.getAnswer());
                    radio.setId(option.getId());
                    group.addView(radio);
                });
                group.setOnCheckedChangeListener((group1, checkedId) -> {
                    answerMultiple.put(question.getQueryId(), checkedId + "");
                });
                layout.addView(group, layoutParams(0, 0));
                break;

            case AGREE:
                RadioGroup group1 = new RadioGroup(this);
                for (int i = 0; i < 5; i++) {
                    RadioButton radio = new RadioButton(this);
                    radio.setText(agrees[i]);
                    radio.setId(i + 1);
                    group1.addView(radio);
                }
                group1.setOnCheckedChangeListener((group2, checkedId) -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("id_questionario", survey.getQuestionId());
                    item.put("value", checkedId + "");
                    answerStar.put(question.getQueryId(), item);
                });
                layout.addView(group1, layoutParams(0, 0));
                break;
        }

        return layout;
    }

    LinearLayout.LayoutParams layoutParams(int w, int h) {
        return new LinearLayout.LayoutParams(w == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : w, h == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : h);
    }

    int starSize() {
        return (container.getWidth() - Elofy.pixel(this, 32) - padding * 20) / 10;
    }
}
