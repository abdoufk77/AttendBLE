package com.example.attendble.ui.prof.classes;

import android.content.Intent;
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

import android.animation.ObjectAnimator;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.usecase.ListClassesByProfWithStatsUseCase;
import com.example.attendble.ui.common.Skeleton;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.example.attendble.ui.prof.profil.ProfilActivity;
import com.example.attendble.ui.prof.serve.ServeHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

/**
 * Écran "My Classes" : liste des classes du professeur connecté.
 * Les stats (room, nb étudiants, taux d'assiduité) ne sont pas encore implémentées —
 * affichées en placeholder en attendant le module Sessions/Pointages.
 */
public class MyClassesActivity extends AppCompatActivity {

    private LinearLayout cardsContainer;
    private View skeleton;
    private ObjectAnimator skeletonPulse;
    private ListClassesByProfWithStatsUseCase listClassesUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_classes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        cardsContainer = findViewById(R.id.cards_container);
        skeleton = findViewById(R.id.skeleton_classes);
        listClassesUseCase = ServiceLocator.provideListClassesByProfWithStatsUseCase();

        findViewById(R.id.card_create_new)
                .setOnClickListener(v -> startActivity(new Intent(this, CreerClasseActivity.class)));
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CreerClasseActivity.class)));

        bindBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        Skeleton.stop(skeletonPulse);
        skeletonPulse = Skeleton.pulse(skeleton);
        skeleton.setVisibility(View.VISIBLE);
        cardsContainer.setVisibility(View.GONE);
        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();
        listClassesUseCase.execute(profId, new Callback<List<ClasseWithAttendance>>() {
            @Override
            public void onSuccess(List<ClasseWithAttendance> rows) {
                renderClasses(rows);
            }

            @Override
            public void onError(Exception e) {
                stopSkeleton();
                Toast.makeText(MyClassesActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void stopSkeleton() {
        Skeleton.stop(skeletonPulse);
        skeleton.setVisibility(View.GONE);
        cardsContainer.setVisibility(View.VISIBLE);
    }

    private void renderClasses(List<ClasseWithAttendance> rows) {
        stopSkeleton();
        cardsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ClasseWithAttendance row : rows) {
            Classe classe = row.getClasse();
            View card = inflater.inflate(R.layout.item_course_card, cardsContainer, false);
            ((TextView) card.findViewById(R.id.tv_course_name))
                    .setText(String.format(Locale.getDefault(), "%s — %s", classe.getNom(), classe.getGroupe()));
            ((TextView) card.findViewById(R.id.tv_course_code)).setText(classe.getCodeInvitation());
            ((TextView) card.findViewById(R.id.tv_course_room)).setText(classe.getSalle());
            ((TextView) card.findViewById(R.id.tv_course_students))
                    .setText(String.format(Locale.getDefault(), "%d étudiants", classe.getNbEtudiants()));

            TextView tvAttendance = card.findViewById(R.id.tv_course_attendance);
            LinearProgressIndicator progress = card.findViewById(R.id.progress_attendance);
            if (row.getNbSessionsFermees() == 0) {
                tvAttendance.setText(R.string.cd_student_no_session);
                progress.setProgressCompat(100, false);
            } else {
                tvAttendance.setText(getString(R.string.student_progress_value_format, row.getTauxPresence()));
                progress.setProgressCompat(row.getTauxPresence(), false);
            }

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, ClassDetailsActivity.class);
                intent.putExtra(ClassDetailsActivity.EXTRA_CLASSE_ID, classe.getClasseId());
                startActivity(intent);
            });
            card.findViewById(R.id.btn_more).setOnClickListener(v -> toast(R.string.mc_toast_more));
            cardsContainer.addView(card);
        }
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_classes);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_serve) {
                startActivity(new Intent(this, ServeHomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_classes) {
                return true;
            }
            toast(R.string.prof_toast_nav);
            return true;
        });
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
