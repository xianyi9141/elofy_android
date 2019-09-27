package com.peatis.elofy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.model.KeyResult;
import com.transitionseverywhere.TransitionManager;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeyResultActivity extends BaseActivity {
    static final String ARG_KEY_RESULT = "key_result";

    @BindView(R.id.textTitle)
    TextView title;

    @BindView(R.id.textUsername)
    TextView username;

    @BindView(R.id.textFrom)
    TextView from;

    @BindView(R.id.textTo)
    TextView to;

    @BindView(R.id.textActual)
    TextView actual;

    @BindView(R.id.textPercentage)
    TextView percent;

    @BindView(R.id.textUnit)
    TextView unit;

    @BindView(R.id.textTimestamp)
    TextView timestamp;

    @BindView(R.id.textDate)
    @NotEmpty(message = "Por favor informe um data.")
    TextInputEditText date;

    @BindView(R.id.textValue)
    @NotEmpty(message = "Por favor informe um valor.")
    TextInputEditText value;

    @BindView(R.id.textDescription)
    EditText description;

    @BindView(R.id.form)
    LinearLayout form;

    @BindView(R.id.btnAdd)
    Button btnAdd;
    KeyResult keyResult;

    public static void startActivity(Context context, KeyResult key) {
        Intent intent = new Intent(context, KeyResultActivity.class);
        intent.putExtra(ARG_KEY_RESULT, key);
        context.startActivity(intent);
    }

    @OnClick(R.id.textDate)
    void onDateClicked() {
        SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(sd.parse(date.getText().toString()).getTime());
        } catch (Exception ignored) {
        }
        new SpinnerDatePickerDialogBuilder()
                .context(this)
                .callback((view, year, monthOfYear, dayOfMonth) -> date.setText(Elofy.dateString("dd/MM/yyyy", year, monthOfYear, dayOfMonth)))
                .defaultDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .build()
                .show();
    }

    @OnClick(R.id.btnAdd)
    void onAddValue() {
        TransitionManager.beginDelayedTransition((ViewGroup) form.getParent());
        btnAdd.setVisibility(View.GONE);

        ViewGroup.LayoutParams layoutParams = form.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        TransitionManager.beginDelayedTransition((ViewGroup) form.getParent());
        form.setLayoutParams(layoutParams);
    }

    @OnClick(R.id.btnSave)
    void onSave() {
        validator.validate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_result);

        showActionBarBack(true);
        title("Medição do key result");
        ButterKnife.bind(this);

        keyResult = (KeyResult) getIntent().getSerializableExtra(ARG_KEY_RESULT);

        title.setText(keyResult.getTitle());
        username.setText(keyResult.getUser().getName() + (keyResult.getUser().getId() == Elofy.Int(this, Elofy.KEY_USER_ID) ? " (Me)" : ""));
        from.setText(keyResult.getFrom() + "");
        to.setText(keyResult.getTo() + "");
        actual.setText(keyResult.getActual() + "");
        percent.setText((int) keyResult.getPercentage() + "%");
        unit.setText(keyResult.getUnit());
        timestamp.setText("Carregando...");

        date.setKeyListener(null);

        loadHistory();
    }

    void loadHistory() {
        getArray("/measurement/" + keyResult.getId(), false, res -> {
            timestamp.setText("n/a");
            Stream.of(res).findFirst().executeIfPresent(elt -> {
                JsonObject json = elt.getAsJsonObject();
                if (json.has("date") && !json.get("date").isJsonNull()) {
                    timestamp.setText(json.get("date").getAsString());
                }
            });
        });
    }

    @Override
    public void onValidationSucceeded() {
        String dateStr = Elofy.dateString(date.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd");
        if (dateStr.equals(date.getText().toString())) {
            message("Formato da data inválido.");
        } else {
            try {
                double val = Double.parseDouble(value.getText().toString());

                Map<String, String> params = new HashMap<>();
                params.put("id", keyResult.getId() + "");
                params.put("date", dateStr);
                params.put("value", val + "");
                params.put("description", description.getText().toString());
                post("/measurement", params, res -> {
                    if (res.get("status").getAsInt() == 1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Parabéns!")
                                .setMessage("Bom trabalho!\nBom trabalho, key result atualizado com sucesso!")
                                .setPositiveButton("Vá para página de objetivos", (dialog, which) -> {
                                    sendBroadcast(new Intent(Elofy.BROADCAST_KEY_VALUE_ADDED));
                                    finish();
                                })
                                .create()
                                .show();

                    } else {
                        message(res.get("message").getAsString());
                    }
                });
            } catch (Exception e) {
                message("Valor deve ser numerico.");
            }
        }
    }
}
