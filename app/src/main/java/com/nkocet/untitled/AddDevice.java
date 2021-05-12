package com.nkocet.untitled;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class AddDevice extends AppCompatActivity {

    // Variable declarations
    private static final String TAG = "AddDevice";
    MaterialButtonToggleGroup buttonToggleGroup;
    TextInputEditText nameEditText, locationEditText, rateEditText, startEditText, endEditText;
    Slider slider;
    MaterialTimePicker timePicker;
    Chip[] chips;
    Vibrator vibrator;
    SharedPreferences preferences;
    ImageView status, back;
    MaterialButton auto, manual;
    TextView title;
    MaterialButton add, cancel;
    boolean haptics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        // Variable initialisations
        nameEditText = findViewById(R.id.editDeviceName);
        locationEditText = findViewById(R.id.editDeviceLocation);
        rateEditText = findViewById(R.id.editFlowRate);
        slider = findViewById(R.id.flowRateSlider);
        add = findViewById(R.id.add);
        cancel = findViewById(R.id.cancel);
        startEditText = findViewById(R.id.editStartTime);
        endEditText = findViewById(R.id.editEndTime);
        status = findViewById(R.id.status);
        back = findViewById(R.id.back);
        title = findViewById(R.id.sprinklerName);
        auto = findViewById(R.id.auto);
        manual = findViewById(R.id.manual);
        auto = findViewById(R.id.auto);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        buttonToggleGroup = findViewById(R.id.buttonGroup);


        chips = new Chip[]{findViewById(R.id.day1),
                findViewById(R.id.day2),
                findViewById(R.id.day3),
                findViewById(R.id.day4),
                findViewById(R.id.day5),
                findViewById(R.id.day6),
                findViewById(R.id.day7)};

        // Database initialisation
        Database database = new Database(getApplicationContext());
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);


        // Setting initial data
        rateEditText.setEnabled(false);
        haptics = preferences.getBoolean("haptics", true);
        manual.setChecked(true);

        // Action Listeners
        back.setOnClickListener(v -> finish());
        cancel.setOnClickListener(v -> finish());
        buttonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> slider.setEnabled(checkedId == manual.getId()));

        startEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePicker(startEditText);
        });
        startEditText.setOnClickListener(v -> showTimePicker(startEditText));

        endEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePicker(endEditText);
        });
        endEditText.setOnClickListener(v -> showTimePicker(endEditText));

        slider.addOnChangeListener((slider, value, fromUser) -> {
            if (value % 5 == 0 && haptics) vibrator.vibrate(30);
            rateEditText.setText(String.valueOf((int) value));
        });

        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                slider.setTrackHeight(50);
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                slider.setTrackHeight(30);
            }
        });

        add.setOnClickListener(v -> {
            // TODO: (data validation required) Save current state and push to sever
            if (TextUtils.isEmpty(nameEditText.getText()))
                nameEditText.setError("This field cannot be left empty!");
            else if (TextUtils.isEmpty(locationEditText.getText()))
                locationEditText.setError("This field cannot be left empty!");
            else {
                String name = nameEditText.getText().toString();
                String location = locationEditText.getText().toString();
                String rate = rateEditText.getText().toString();
                int[] activeDays = new int[chips.length];
                for (int i = 0; i < chips.length; i++) activeDays[i] = chips[i].isChecked() ? 1 : 0;

                String start = startEditText.getText().toString();
                start = start.equals("") ? null : start;
                String end = endEditText.getText().toString();
                end = end.equals("") ? null : end;

                Sprinkler sprinkler = new Sprinkler(1, Integer.parseInt(rate), activeDays, new String[]{start, end}, auto.isChecked());
                int nextId = Integer.parseInt(database.getLastId()) + 1;
                Card card = new Card(String.valueOf(nextId), name, location, new String[]{}, sprinkler);
                database.insertCard(card);
                setResult(2);
                finish();
            }
        });
    }

    public void showTimePicker(TextInputEditText editText) {
        timePicker = new MaterialTimePicker.Builder()
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Choose time")
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build();
        timePicker.show(getSupportFragmentManager(), "Time picker");
        timePicker.addOnPositiveButtonClickListener(v -> {
            String hours = String.valueOf(timePicker.getHour()),
                    minutes = String.valueOf(timePicker.getMinute());
            editText.setText(String.format("%s:%s", hours, minutes));
        });
    }
}