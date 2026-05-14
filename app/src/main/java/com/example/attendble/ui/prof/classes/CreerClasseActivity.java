package com.example.attendble.ui.prof.classes;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.usecase.CreerClasseUseCase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Formulaire de création d'une classe. Horaire saisi via Material DatePicker (date → jourSemaine)
 * + Material TimePickers (heureDebut / heureFin). Le {@code codeInvitation} est généré côté repo.
 */
public class CreerClasseActivity extends AppCompatActivity {

    private TextInputLayout tilCourseName, tilSubject, tilGroup, tilRoom,
            tilScheduleDate, tilScheduleStart, tilScheduleEnd, tilTotalStudents;
    private TextInputEditText etCourseName, etSubject, etGroup, etRoom,
            etScheduleDate, etScheduleStart, etScheduleEnd, etTotalStudents;
    private MaterialButton btnCreate;

    private CreerClasseUseCase creerClasseUseCase;

    private Long selectedDateUtcMillis;
    private Integer jourSemaineIso;
    private Integer startHour, startMinute, endHour, endMinute;

    private final SimpleDateFormat displayDateFmt = new SimpleDateFormat("EEEE d MMM yyyy", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_creer_classe);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        creerClasseUseCase = ServiceLocator.provideCreerClasseUseCase();

        bindViews();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> attemptCreate());

        etScheduleDate.setOnClickListener(v -> showDatePicker());
        tilScheduleDate.setEndIconOnClickListener(v -> showDatePicker());
        etScheduleStart.setOnClickListener(v -> showTimePicker(true));
        tilScheduleStart.setEndIconOnClickListener(v -> showTimePicker(true));
        etScheduleEnd.setOnClickListener(v -> showTimePicker(false));
        tilScheduleEnd.setEndIconOnClickListener(v -> showTimePicker(false));
    }

    private void bindViews() {
        tilCourseName = findViewById(R.id.til_course_name);
        tilSubject = findViewById(R.id.til_subject);
        tilGroup = findViewById(R.id.til_group);
        tilRoom = findViewById(R.id.til_room);
        tilScheduleDate = findViewById(R.id.til_schedule_date);
        tilScheduleStart = findViewById(R.id.til_schedule_start);
        tilScheduleEnd = findViewById(R.id.til_schedule_end);
        tilTotalStudents = findViewById(R.id.til_total_students);

        etCourseName = findViewById(R.id.et_course_name);
        etSubject = findViewById(R.id.et_subject);
        etGroup = findViewById(R.id.et_group);
        etRoom = findViewById(R.id.et_room);
        etScheduleDate = findViewById(R.id.et_schedule_date);
        etScheduleStart = findViewById(R.id.et_schedule_start);
        etScheduleEnd = findViewById(R.id.et_schedule_end);
        etTotalStudents = findViewById(R.id.et_total_students);

        btnCreate = findViewById(R.id.btn_create);
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.cc_picker_date_title);
        if (selectedDateUtcMillis != null) builder.setSelection(selectedDateUtcMillis);
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(utcMillis -> {
            selectedDateUtcMillis = utcMillis;
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(utcMillis);
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            jourSemaineIso = dow == Calendar.SUNDAY ? 7 : dow - 1;
            displayDateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            etScheduleDate.setText(displayDateFmt.format(new Date(utcMillis)));
            tilScheduleDate.setError(null);
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void showTimePicker(boolean isStart) {
        int hour = isStart ? (startHour != null ? startHour : 9)
                : (endHour != null ? endHour : 11);
        int minute = isStart ? (startMinute != null ? startMinute : 0)
                : (endMinute != null ? endMinute : 0);
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isStart ? R.string.cc_picker_start_title : R.string.cc_picker_end_title)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            int h = picker.getHour();
            int m = picker.getMinute();
            String formatted = String.format(Locale.ROOT, "%02d:%02d", h, m);
            if (isStart) {
                startHour = h; startMinute = m;
                etScheduleStart.setText(formatted);
                tilScheduleStart.setError(null);
            } else {
                endHour = h; endMinute = m;
                etScheduleEnd.setText(formatted);
                tilScheduleEnd.setError(null);
            }
        });
        picker.show(getSupportFragmentManager(), isStart ? "start_picker" : "end_picker");
    }

    private void attemptCreate() {
        clearErrors();

        String nom = textOf(etCourseName);
        String matiere = textOf(etSubject);
        String groupe = textOf(etGroup);
        String salle = textOf(etRoom);
        String nbEtu = textOf(etTotalStudents);

        boolean valid = true;
        if (TextUtils.isEmpty(nom)) { tilCourseName.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(matiere)) { tilSubject.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(groupe)) { tilGroup.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(salle)) { tilRoom.setError(getString(R.string.cc_error_required)); valid = false; }
        if (jourSemaineIso == null) { tilScheduleDate.setError(getString(R.string.cc_error_required)); valid = false; }
        if (startHour == null) { tilScheduleStart.setError(getString(R.string.cc_error_required)); valid = false; }
        if (endHour == null) { tilScheduleEnd.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(nbEtu)) { tilTotalStudents.setError(getString(R.string.cc_error_required)); valid = false; }
        if (!valid) return;

        int startMin = startHour * 60 + startMinute;
        int endMin = endHour * 60 + endMinute;
        if (endMin <= startMin) {
            tilScheduleEnd.setError(getString(R.string.cc_error_end_before_start));
            return;
        }

        String heureDebut = String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute);
        String heureFin = String.format(Locale.ROOT, "%02d:%02d", endHour, endMinute);

        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();
        btnCreate.setEnabled(false);
        creerClasseUseCase.execute(nom, matiere, groupe, salle,
                jourSemaineIso, heureDebut, heureFin, nbEtu, profId,
                new Callback<Classe>() {
                    @Override
                    public void onSuccess(Classe classe) {
                        btnCreate.setEnabled(true);
                        Toast.makeText(CreerClasseActivity.this,
                                getString(R.string.cc_created_format, classe.getCodeInvitation()),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        btnCreate.setEnabled(true);
                        Toast.makeText(CreerClasseActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilCourseName.setError(null);
        tilSubject.setError(null);
        tilGroup.setError(null);
        tilRoom.setError(null);
        tilScheduleDate.setError(null);
        tilScheduleStart.setError(null);
        tilScheduleEnd.setError(null);
        tilTotalStudents.setError(null);
    }
}
