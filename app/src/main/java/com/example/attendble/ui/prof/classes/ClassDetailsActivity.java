package com.example.attendble.ui.prof.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.example.attendble.domain.model.EtudiantAttendance;
import com.example.attendble.domain.usecase.GetClasseUseCase;
import com.example.attendble.domain.usecase.ListEtudiantsByClasseUseCase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

/**
 * Détail d'une classe : hero (nom/groupe/salle), code invitation (réel),
 * liste des étudiants inscrits (démo — sera branché plus tard sur Pointage/Inscriptions).
 */
public class ClassDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CLASSE_ID = "extra_classe_id";

    private TextView tvClassName, tvClassGroup, tvClassRoom, tvInvitationCode, tvStudentsEmpty;
    private LinearLayout studentsContainer;
    private String classeId;
    private GetClasseUseCase getClasseUseCase;
    private ListEtudiantsByClasseUseCase listEtudiantsUseCase;

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
        tvStudentsEmpty = findViewById(R.id.tv_students_empty);
        studentsContainer = findViewById(R.id.students_container);

        getClasseUseCase = ServiceLocator.provideGetClasseUseCase();
        listEtudiantsUseCase = ServiceLocator.provideListEtudiantsByClasseUseCase();

        classeId = getIntent().getStringExtra(EXTRA_CLASSE_ID);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_copy).setOnClickListener(v -> copyInvitationCode());
        findViewById(R.id.btn_share).setOnClickListener(v -> toast(R.string.cd_toast_share));
        ((MaterialButton) findViewById(R.id.btn_invite)).setOnClickListener(v -> toast(R.string.cd_toast_invite));

        loadClasse();
        loadStudents();
    }

    private void loadClasse() {
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

    private void loadStudents() {
        listEtudiantsUseCase.execute(classeId, new Callback<List<EtudiantAttendance>>() {
            @Override
            public void onSuccess(List<EtudiantAttendance> rows) {
                renderStudents(rows);
            }

            @Override
            public void onError(Exception e) {
                renderStudents(java.util.Collections.emptyList());
                Toast.makeText(ClassDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderStudents(List<EtudiantAttendance> rows) {
        studentsContainer.removeAllViews();
        if (rows.isEmpty()) {
            tvStudentsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvStudentsEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (EtudiantAttendance row : rows) {
            View item = inflater.inflate(R.layout.item_student_row, studentsContainer, false);
            ((TextView) item.findViewById(R.id.tv_student_name)).setText(row.getEtudiant().getNom());
            int taux = row.getTauxPresence();
            TextView tvAttendance = item.findViewById(R.id.tv_student_attendance);
            if (row.getNbSessionsFermees() == 0) {
                tvAttendance.setText(R.string.cd_student_no_session);
            } else {
                tvAttendance.setText(getString(R.string.cd_student_attendance_format,
                        taux, row.getNbPresences(), row.getNbSessionsFermees()));
            }
            ((LinearProgressIndicator) item.findViewById(R.id.progress_student))
                    .setProgressCompat(taux, false);

            item.setOnClickListener(v -> toast(R.string.cd_toast_student_clicked));
            item.findViewById(R.id.btn_student_more).setOnClickListener(v -> toast(R.string.cd_toast_student_more));
            studentsContainer.addView(item);
        }
    }

    private void copyInvitationCode() {
        CharSequence code = tvInvitationCode.getText();
        if (code == null || code.length() == 0) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("invitation_code", code));
        toast(R.string.cd_toast_copy);
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
