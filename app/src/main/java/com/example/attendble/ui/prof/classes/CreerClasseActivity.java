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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Formulaire de création d'une nouvelle classe.
 * Stubé — à brancher via un futur CreerClasseUseCase + ClasseRepository.
 */
public class CreerClasseActivity extends AppCompatActivity {

    private TextInputLayout tilCourseName, tilSubject, tilGroup, tilRoom, tilSchedule, tilTotalStudents;
    private TextInputEditText etCourseName, etSubject, etGroup, etRoom, etSchedule, etTotalStudents;
    private MaterialButton btnCreate;

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

        bindViews();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> attemptCreate());
    }

    private void bindViews() {
        tilCourseName = findViewById(R.id.til_course_name);
        tilSubject = findViewById(R.id.til_subject);
        tilGroup = findViewById(R.id.til_group);
        tilRoom = findViewById(R.id.til_room);
        tilSchedule = findViewById(R.id.til_schedule);
        tilTotalStudents = findViewById(R.id.til_total_students);

        etCourseName = findViewById(R.id.et_course_name);
        etSubject = findViewById(R.id.et_subject);
        etGroup = findViewById(R.id.et_group);
        etRoom = findViewById(R.id.et_room);
        etSchedule = findViewById(R.id.et_schedule);
        etTotalStudents = findViewById(R.id.et_total_students);

        btnCreate = findViewById(R.id.btn_create);
    }

    private void attemptCreate() {
        clearErrors();

        boolean valid = true;
        valid &= validateRequired(tilCourseName, etCourseName);
        valid &= validateRequired(tilSubject, etSubject);
        valid &= validateRequired(tilGroup, etGroup);
        valid &= validateRequired(tilRoom, etRoom);
        valid &= validateRequired(tilSchedule, etSchedule);
        valid &= validateRequired(tilTotalStudents, etTotalStudents);

        if (!valid) return;

        Toast.makeText(this, R.string.cc_toast_create, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validateRequired(TextInputLayout til, TextInputEditText et) {
        String value = et.getText() != null ? et.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            til.setError(getString(R.string.cc_error_required));
            return false;
        }
        return true;
    }

    private void clearErrors() {
        tilCourseName.setError(null);
        tilSubject.setError(null);
        tilGroup.setError(null);
        tilRoom.setError(null);
        tilSchedule.setError(null);
        tilTotalStudents.setError(null);
    }
}
