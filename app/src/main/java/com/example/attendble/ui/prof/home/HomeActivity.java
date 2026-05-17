package com.example.attendble.ui.prof.home;

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
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.domain.model.ProfStats;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.common.Skeleton;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.example.attendble.ui.prof.profil.ProfilActivity;
import com.example.attendble.ui.prof.serve.ActiveSessionActivity;
import com.example.attendble.ui.prof.serve.ServeHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/** Écran d'accueil du prof : greeting + cours du jour (filtrés sur jourSemaine = aujourd'hui). */
public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvLecturesCount;
    private TextView tvEmpty;
    private TextView tvTotalStudentsValue;
    private TextView tvTotalStudentsChip;
    private TextView tvAvgAttendanceValue;
    private TextView tvAvgAttendanceChip;
    private LinearLayout todayContainer;
    private View todayScroll;
    private View skeletonToday;
    private View statsRow;
    private View skeletonStats;
    private ObjectAnimator todayPulse;
    private ObjectAnimator statsPulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvGreeting = findViewById(R.id.tv_greeting);
        tvLecturesCount = findViewById(R.id.tv_lectures_count);
        tvEmpty = findViewById(R.id.tv_today_empty);
        tvTotalStudentsValue = findViewById(R.id.tv_total_students_value);
        tvTotalStudentsChip = findViewById(R.id.tv_total_students_chip);
        tvAvgAttendanceValue = findViewById(R.id.tv_avg_attendance_value);
        tvAvgAttendanceChip = findViewById(R.id.tv_avg_attendance_chip);
        todayContainer = findViewById(R.id.today_container);
        todayScroll = findViewById(R.id.today_scroll);
        skeletonToday = findViewById(R.id.skeleton_today);
        statsRow = findViewById(R.id.stats_row);
        skeletonStats = findViewById(R.id.skeleton_stats);

        findViewById(R.id.btn_view_all).setOnClickListener(v ->
                startActivity(new Intent(this, MyClassesActivity.class)));

        bindBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGreeting();
        loadStats();
        startSkeleton();
        loadTodaySchedule();
    }

    private void startSkeleton() {
        skeletonToday.setVisibility(View.VISIBLE);
        todayScroll.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        Skeleton.stop(todayPulse);
        todayPulse = Skeleton.pulse(skeletonToday);
    }

    private void stopSkeleton() {
        Skeleton.stop(todayPulse);
        skeletonToday.setVisibility(View.GONE);
        todayScroll.setVisibility(View.VISIBLE);
    }

    private void loadStats() {
        Skeleton.stop(statsPulse);
        statsPulse = Skeleton.pulse(skeletonStats);
        skeletonStats.setVisibility(View.VISIBLE);
        statsRow.setVisibility(View.GONE);
        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideGetProfStatsUseCase().execute(profId, new Callback<ProfStats>() {
            @Override
            public void onSuccess(ProfStats stats) {
                stopStatsSkeleton();
                tvTotalStudentsValue.setText(String.valueOf(stats.getTotalStudents()));
                tvTotalStudentsChip.setText(getString(R.string.home_stat_classes_chip, stats.getTotalClasses()));
                tvAvgAttendanceValue.setText(getString(R.string.student_progress_value_format, stats.getAvgAttendance()));
                if (stats.getAvgAttendance() == 100 && stats.getTotalStudents() > 0) {
                    tvAvgAttendanceChip.setText(R.string.home_stat_no_session_chip);
                } else {
                    tvAvgAttendanceChip.setText(R.string.home_stat_avg_chip);
                }
            }

            @Override
            public void onError(Exception e) {
                stopStatsSkeleton();
            }
        });
    }

    private void stopStatsSkeleton() {
        Skeleton.stop(statsPulse);
        skeletonStats.setVisibility(View.GONE);
        statsRow.setVisibility(View.VISIBLE);
    }

    private void loadGreeting() {
        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                tvGreeting.setText(getString(R.string.home_greeting_format, user.getNom()));
            }

            @Override
            public void onError(Exception e) { /* keep default */ }
        });
    }

    private void loadTodaySchedule() {
        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListTodayCoursesUseCase().execute(profId, UserRole.PROFESSEUR,
                new Callback<List<Classe>>() {
                    @Override
                    public void onSuccess(List<Classe> classes) {
                        renderToday(classes);
                    }

                    @Override
                    public void onError(Exception e) {
                        renderToday(java.util.Collections.emptyList());
                    }
                });
    }

    private void renderToday(List<Classe> classes) {
        stopSkeleton();
        todayContainer.removeAllViews();
        int count = classes.size();
        String unit = getString(count == 1
                ? R.string.home_lectures_count_singular
                : R.string.home_lectures_count_plural);
        tvLecturesCount.setText(getString(R.string.home_lectures_count_format, count, unit));

        if (classes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < classes.size(); i++) {
            Classe c = classes.get(i);
            MaterialCardView card = (MaterialCardView) inflater.inflate(
                    R.layout.item_today_course_prof, todayContainer, false);
            if (i > 0) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
                lp.setMarginStart((int) (12 * getResources().getDisplayMetrics().density));
                card.setLayoutParams(lp);
            }
            ((TextView) card.findViewById(R.id.tv_course_name)).setText(c.getNom());
            ((TextView) card.findViewById(R.id.tv_course_group)).setText(c.getGroupe());
            ((TextView) card.findViewById(R.id.tv_course_room)).setText(c.getSalle());
            ((TextView) card.findViewById(R.id.tv_course_time)).setText(
                    c.getHeureDebut() + " - " + c.getHeureFin());

            MaterialButton btn = card.findViewById(R.id.btn_start_session);
            final String classeId = c.getClasseId();
            btn.setOnClickListener(v -> {
                Intent i2 = new Intent(this, ActiveSessionActivity.class);
                i2.putExtra("classeId", classeId);
                startActivity(i2);
            });
            todayContainer.addView(card);
        }
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            }
            if (id == R.id.nav_classes) {
                startActivity(new Intent(this, MyClassesActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_serve) {
                startActivity(new Intent(this, ServeHomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilActivity.class));
                finish();
                return true;
            }
            Toast.makeText(this, R.string.prof_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
