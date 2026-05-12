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
import com.example.attendble.ui.student.home.HomeActivity;
import com.example.attendble.ui.student.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Écran "Mes classes" étudiant : liste des classes rejointes via code d'invitation
 * (workflow Google Classroom). Cartes inflées depuis item_course_card. Stubé —
 * à brancher via ClasseRepository + GetEnrolledClassesUseCase.
 */
public class MyClassesActivity extends AppCompatActivity {

    private static class ClasseStub {
        final int name, meta, room, attendance, progress;

        ClasseStub(int name, int meta, int room, int attendance, int progress) {
            this.name = name;
            this.meta = meta;
            this.room = room;
            this.attendance = attendance;
            this.progress = progress;
        }
    }

    private final ClasseStub[] stubClasses = new ClasseStub[]{
            new ClasseStub(R.string.smc_class1_name, R.string.smc_class1_meta,
                    R.string.smc_class1_room, R.string.smc_class1_attendance, 92),
            new ClasseStub(R.string.smc_class2_name, R.string.smc_class2_meta,
                    R.string.smc_class2_room, R.string.smc_class2_attendance, 64),
            new ClasseStub(R.string.smc_class3_name, R.string.smc_class3_meta,
                    R.string.smc_class3_room, R.string.smc_class3_attendance, 88)
    };

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

        populateClassCards();
        bindJoin();
        bindBottomNav();
    }

    private void populateClassCards() {
        LinearLayout container = findViewById(R.id.cards_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ClasseStub c : stubClasses) {
            View card = inflater.inflate(R.layout.item_course_card, container, false);
            ((TextView) card.findViewById(R.id.tv_course_name)).setText(c.name);
            ((TextView) card.findViewById(R.id.tv_course_code)).setText(c.meta);
            ((TextView) card.findViewById(R.id.tv_course_room)).setText(c.room);
            ((TextView) card.findViewById(R.id.tv_course_attendance)).setText(c.attendance);
            ((LinearProgressIndicator) card.findViewById(R.id.progress_attendance))
                    .setProgressCompat(c.progress, false);

            // Le côté étudiant n'affiche pas le nombre d'inscrits : on masque la ligne.
            View studentsRow = (View) card.findViewById(R.id.tv_course_students).getParent();
            studentsRow.setVisibility(View.GONE);

            card.findViewById(R.id.btn_more).setOnClickListener(v -> toast(R.string.smc_toast_more));
            card.setOnClickListener(v -> toast(R.string.smc_toast_class_clicked));
            container.addView(card);
        }
    }

    private void bindJoin() {
        View.OnClickListener joinListener = v -> toast(R.string.smc_toast_join);
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
