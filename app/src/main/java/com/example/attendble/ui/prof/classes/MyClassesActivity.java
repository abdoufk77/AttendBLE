package com.example.attendble.ui.prof.classes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.prof.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Écran "My Classes" : liste des cours gérés par le professeur (cartes avec stats).
 * Les données sont stubées — à brancher via un futur ClasseRepository / ListClassesUseCase.
 */
public class MyClassesActivity extends AppCompatActivity {

    private static class CourseStub {
        final int name, code, room, students, attendance, progress;

        CourseStub(int name, int code, int room, int students, int attendance, int progress) {
            this.name = name;
            this.code = code;
            this.room = room;
            this.students = students;
            this.attendance = attendance;
            this.progress = progress;
        }
    }

    private final CourseStub[] stubCourses = new CourseStub[]{
            new CourseStub(R.string.mc_course1_name, R.string.mc_course1_code, R.string.mc_course1_room,
                    R.string.mc_course1_students, R.string.mc_course1_attendance, 92),
            new CourseStub(R.string.mc_course2_name, R.string.mc_course2_code, R.string.mc_course2_room,
                    R.string.mc_course2_students, R.string.mc_course2_attendance, 85),
            new CourseStub(R.string.mc_course3_name, R.string.mc_course3_code, R.string.mc_course3_room,
                    R.string.mc_course3_students, R.string.mc_course3_attendance, 78),
            new CourseStub(R.string.mc_course4_name, R.string.mc_course4_code, R.string.mc_course4_room,
                    R.string.mc_course4_students, R.string.mc_course4_attendance, 95),
            new CourseStub(R.string.mc_course5_name, R.string.mc_course5_code, R.string.mc_course5_room,
                    R.string.mc_course5_students, R.string.mc_course5_attendance, 88),
            new CourseStub(R.string.mc_course6_name, R.string.mc_course6_code, R.string.mc_course6_room,
                    R.string.mc_course6_students, R.string.mc_course6_attendance, 73)
    };

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

        populateCourseCards();
        bindPlaceholder();
        bindFab();
        bindBottomNav();
    }

    private void populateCourseCards() {
        LinearLayout container = findViewById(R.id.cards_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (CourseStub course : stubCourses) {
            View card = inflater.inflate(R.layout.item_course_card, container, false);
            ((TextView) card.findViewById(R.id.tv_course_name)).setText(course.name);
            ((TextView) card.findViewById(R.id.tv_course_code)).setText(course.code);
            ((TextView) card.findViewById(R.id.tv_course_room)).setText(course.room);
            ((TextView) card.findViewById(R.id.tv_course_students)).setText(course.students);
            ((TextView) card.findViewById(R.id.tv_course_attendance)).setText(course.attendance);
            ((LinearProgressIndicator) card.findViewById(R.id.progress_attendance))
                    .setProgressCompat(course.progress, false);

            card.setOnClickListener(v -> startActivity(new Intent(this, ClassDetailsActivity.class)));
            card.findViewById(R.id.btn_more).setOnClickListener(v -> toast(R.string.mc_toast_more));
            container.addView(card);
        }
    }

    private void bindPlaceholder() {
        findViewById(R.id.card_create_new)
                .setOnClickListener(v -> startActivity(new Intent(this, CreerClasseActivity.class)));
    }

    private void bindFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CreerClasseActivity.class)));
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_classes);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
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
