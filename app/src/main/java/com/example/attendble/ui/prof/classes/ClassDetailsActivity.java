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
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.usecase.GetClasseUseCase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * Détail d'une classe : hero (nom/groupe/salle), code invitation (réel),
 * liste des étudiants inscrits (démo — sera branché plus tard sur Pointage/Inscriptions).
 */
public class ClassDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CLASSE_ID = "extra_classe_id";

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

    private TextView tvClassName, tvClassGroup, tvClassRoom, tvInvitationCode;
    private GetClasseUseCase getClasseUseCase;

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

        tvClassName = findViewById(R.id.tv_class_name);
        tvClassGroup = findViewById(R.id.tv_class_group);
        tvClassRoom = findViewById(R.id.tv_class_room);
        tvInvitationCode = findViewById(R.id.tv_invitation_code);

        getClasseUseCase = ServiceLocator.provideGetClasseUseCase();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_copy).setOnClickListener(v -> toast(R.string.cd_toast_copy));
        findViewById(R.id.btn_share).setOnClickListener(v -> toast(R.string.cd_toast_share));
        ((MaterialButton) findViewById(R.id.btn_invite)).setOnClickListener(v -> toast(R.string.cd_toast_invite));

        loadClasse();
        populateStudents();
    }

    private void loadClasse() {
        String classeId = getIntent().getStringExtra(EXTRA_CLASSE_ID);
        getClasseUseCase.execute(classeId, new Callback<Classe>() {
            @Override
            public void onSuccess(Classe classe) {
                tvClassName.setText(classe.getNom());
                tvClassGroup.setText(String.format(Locale.getDefault(), "%s · %s",
                        classe.getMatiere(), classe.getGroupe()));
                tvClassRoom.setText(String.format(Locale.getDefault(), "%s · %s",
                        classe.getSalle(), classe.getHoraire()));
                tvInvitationCode.setText(classe.getCodeInvitation());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ClassDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
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
