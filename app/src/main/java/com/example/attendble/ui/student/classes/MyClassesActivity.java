package com.example.attendble.ui.student.classes;

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

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.domain.Callback;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.usecase.ListClassesByEtudiantUseCase;
import com.example.attendble.ui.student.home.HomeActivity;
import com.example.attendble.ui.student.profil.ProfilActivity;
import com.example.attendble.ui.student.scan.SearchingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

/**
 * Écran "Mes classes" étudiant : liste les classes rejointes via code d'invitation.
 * Une carte démo est conservée en tête pour le visuel ; les vraies classes suivent.
 */
public class MyClassesActivity extends AppCompatActivity {

    private LinearLayout cardsContainer;
    private ListClassesByEtudiantUseCase listClassesUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_my_classes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        cardsContainer = findViewById(R.id.cards_container);
        listClassesUseCase = ServiceLocator.provideListClassesByEtudiantUseCase();

        bindJoin();
        bindBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        String etudiantId = ServiceLocator.getAuthRepository().getCurrentUserId();
        listClassesUseCase.execute(etudiantId, new Callback<List<Classe>>() {
            @Override
            public void onSuccess(List<Classe> classes) {
                renderClasses(classes);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MyClassesActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderClasses(List<Classe> classes) {
        cardsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        // Une seule carte démo conservée en tête (visuel — sera retirée quand le module sera complet).
        addDemoCard(inflater);

        for (Classe classe : classes) {
            View card = inflater.inflate(R.layout.item_course_card, cardsContainer, false);
            ((TextView) card.findViewById(R.id.tv_course_name)).setText(classe.getNom());
            ((TextView) card.findViewById(R.id.tv_course_code))
                    .setText(String.format(Locale.getDefault(), "%s · %s",
                            classe.getMatiere(), classe.getGroupe()));
            ((TextView) card.findViewById(R.id.tv_course_room))
                    .setText(String.format(Locale.getDefault(), "%s · %s",
                            classe.getSalle(), classe.getHoraire()));
            ((TextView) card.findViewById(R.id.tv_course_attendance)).setText("—");
            ((LinearProgressIndicator) card.findViewById(R.id.progress_attendance))
                    .setProgressCompat(0, false);

            // Côté étudiant : on masque le compteur d'inscrits.
            View studentsRow = (View) card.findViewById(R.id.tv_course_students).getParent();
            studentsRow.setVisibility(View.GONE);

            card.findViewById(R.id.btn_more).setOnClickListener(v -> toast(R.string.smc_toast_more));
            card.setOnClickListener(v -> toast(R.string.smc_toast_class_clicked));
            cardsContainer.addView(card);
        }
    }

    private void addDemoCard(LayoutInflater inflater) {
        View card = inflater.inflate(R.layout.item_course_card, cardsContainer, false);
        ((TextView) card.findViewById(R.id.tv_course_name)).setText(R.string.smc_class1_name);
        ((TextView) card.findViewById(R.id.tv_course_code)).setText(R.string.smc_class1_meta);
        ((TextView) card.findViewById(R.id.tv_course_room)).setText(R.string.smc_class1_room);
        ((TextView) card.findViewById(R.id.tv_course_attendance)).setText(R.string.smc_class1_attendance);
        ((LinearProgressIndicator) card.findViewById(R.id.progress_attendance))
                .setProgressCompat(92, false);

        View studentsRow = (View) card.findViewById(R.id.tv_course_students).getParent();
        studentsRow.setVisibility(View.GONE);

        card.findViewById(R.id.btn_more).setOnClickListener(v -> toast(R.string.smc_toast_more));
        card.setOnClickListener(v -> toast(R.string.smc_toast_class_clicked));
        cardsContainer.addView(card);
    }

    private void bindJoin() {
        View.OnClickListener joinListener = v ->
                startActivity(new Intent(this, JoinClassActivity.class));
        findViewById(R.id.card_join).setOnClickListener(joinListener);
        FloatingActionButton fab = findViewById(R.id.fab_join);
        fab.setOnClickListener(joinListener);
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_classes);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_classes) {
                return true;
            }
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_scan) {
                startActivity(new Intent(this, SearchingActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
                finish();
                return true;
            }
            toast(R.string.student_toast_nav);
            return true;
        });
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
