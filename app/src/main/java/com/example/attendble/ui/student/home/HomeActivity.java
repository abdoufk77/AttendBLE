package com.example.attendble.ui.student.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Écran d'accueil de l'étudiant : greeting, avertissement d'absence, cours du jour
 * et progression de présence. Stubé — à brancher via GetCurrentUserUseCase +
 * GetTodayCoursesUseCase + GetAttendanceStatsUseCase.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btn_view_schedule).setOnClickListener(v ->
                Toast.makeText(this, R.string.student_toast_view_schedule, Toast.LENGTH_SHORT).show());

        FloatingActionButton fab = findViewById(R.id.fab_scan);
        fab.setOnClickListener(v ->
                Toast.makeText(this, R.string.student_toast_scan, Toast.LENGTH_SHORT).show());

        bindBottomNav();
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            }
            Toast.makeText(this, R.string.student_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
