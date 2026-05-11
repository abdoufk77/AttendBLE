package com.example.attendble.ui.prof.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Détail d'une classe : hero (nom/groupe/salle), code invitation, liste des étudiants inscrits.
 * Stubé — à brancher via ClasseRepository + ListEtudiantsUseCase.
 */
public class ClassDetailsActivity extends AppCompatActivity {

    private static class StudentStub {
        final int name, attendance, progress;

        StudentStub(int name, int attendance, int progress) {
            this.name = name;
            this.attendance = attendance;
            this.progress = progress;
        }
    }

    private final StudentStub[] stubStudents = new StudentStub[]{
            new StudentStub(R.string.cd_student1_name, R.string.cd_student1_attendance, 96),
            new StudentStub(R.string.cd_student2_name, R.string.cd_student2_attendance, 88),
            new StudentStub(R.string.cd_student3_name, R.string.cd_student3_attendance, 92)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_copy).setOnClickListener(v -> toast(R.string.cd_toast_copy));
        findViewById(R.id.btn_share).setOnClickListener(v -> toast(R.string.cd_toast_share));
        ((MaterialButton) findViewById(R.id.btn_invite)).setOnClickListener(v -> toast(R.string.cd_toast_invite));

        populateStudents();
    }

    private void populateStudents() {
        LinearLayout container = findViewById(R.id.students_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (StudentStub student : stubStudents) {
            View row = inflater.inflate(R.layout.item_student_row, container, false);
            ((TextView) row.findViewById(R.id.tv_student_name)).setText(student.name);
            ((TextView) row.findViewById(R.id.tv_student_attendance)).setText(student.attendance);
            ((LinearProgressIndicator) row.findViewById(R.id.progress_student))
                    .setProgressCompat(student.progress, false);

            row.setOnClickListener(v -> toast(R.string.cd_toast_student_clicked));
            row.findViewById(R.id.btn_student_more).setOnClickListener(v -> toast(R.string.cd_toast_student_more));
            container.addView(row);
        }
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
