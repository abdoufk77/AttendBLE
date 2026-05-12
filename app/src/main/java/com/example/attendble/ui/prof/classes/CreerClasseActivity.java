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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Formulaire de création d'une classe. Le {@code codeInvitation} est généré et garanti
 * unique par le repository — l'UI n'a rien à faire pour ça.
 */
public class CreerClasseActivity extends AppCompatActivity {

    private TextInputLayout tilCourseName, tilSubject, tilGroup, tilRoom, tilSchedule, tilTotalStudents;
    private TextInputEditText etCourseName, etSubject, etGroup, etRoom, etSchedule, etTotalStudents;
    private MaterialButton btnCreate;

    private CreerClasseUseCase creerClasseUseCase;

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

        String nom = textOf(etCourseName);
        String matiere = textOf(etSubject);
        String groupe = textOf(etGroup);
        String salle = textOf(etRoom);
        String horaire = textOf(etSchedule);
        String nbEtu = textOf(etTotalStudents);

        boolean valid = true;
        if (TextUtils.isEmpty(nom)) { tilCourseName.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(matiere)) { tilSubject.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(groupe)) { tilGroup.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(salle)) { tilRoom.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(horaire)) { tilSchedule.setError(getString(R.string.cc_error_required)); valid = false; }
        if (TextUtils.isEmpty(nbEtu)) { tilTotalStudents.setError(getString(R.string.cc_error_required)); valid = false; }
        if (!valid) return;

        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();

        btnCreate.setEnabled(false);
        creerClasseUseCase.execute(nom, matiere, groupe, salle, horaire, nbEtu, profId,
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
        tilSchedule.setError(null);
        tilTotalStudents.setError(null);
    }
}
