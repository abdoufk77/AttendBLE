package com.example.attendble.ui.student.home;

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
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.common.Skeleton;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.profil.ProfilActivity;
import com.example.attendble.ui.student.scan.SearchingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

/** Écran d'accueil étudiant : greeting + cours du jour (filtrés sur jourSemaine = aujourd'hui). */
public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvEmpty;
    private TextView tvProgressEmpty;
    private LinearLayout todayContainer;
    private LinearLayout progressContainer;
    private View skeletonToday;
    private View skeletonProgress;
    private ObjectAnimator todayPulse;
    private ObjectAnimator progressPulse;

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

        tvGreeting = findViewById(R.id.tv_greeting);
        tvEmpty = findViewById(R.id.tv_today_empty);
        tvProgressEmpty = findViewById(R.id.tv_progress_empty);
        todayContainer = findViewById(R.id.today_container);
        progressContainer = findViewById(R.id.progress_container);
        skeletonToday = findViewById(R.id.skeleton_today);
        skeletonProgress = findViewById(R.id.skeleton_progress);

        findViewById(R.id.btn_view_schedule).setOnClickListener(v ->
                Toast.makeText(this, R.string.student_toast_view_schedule, Toast.LENGTH_SHORT).show());

        FloatingActionButton fab = findViewById(R.id.fab_scan);
        fab.setOnClickListener(v -> startActivity(new Intent(this, SearchingActivity.class)));

        bindBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGreeting();
        startSkeletons();
        loadTodaySchedule();
        loadProgress();
    }

    private void startSkeletons() {
        skeletonToday.setVisibility(View.VISIBLE);
        todayContainer.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        Skeleton.stop(todayPulse);
        todayPulse = Skeleton.pulse(skeletonToday);

        skeletonProgress.setVisibility(View.VISIBLE);
        progressContainer.setVisibility(View.GONE);
        tvProgressEmpty.setVisibility(View.GONE);
        Skeleton.stop(progressPulse);
        progressPulse = Skeleton.pulse(skeletonProgress);
    }

    private void stopSkeletonToday() {
        Skeleton.stop(todayPulse);
        skeletonToday.setVisibility(View.GONE);
        todayContainer.setVisibility(View.VISIBLE);
    }

    private void stopSkeletonProgress() {
        Skeleton.stop(progressPulse);
        skeletonProgress.setVisibility(View.GONE);
        progressContainer.setVisibility(View.VISIBLE);
    }

    private void loadProgress() {
        String userId = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListClassesByEtudiantWithStatsUseCase().execute(userId,
                new Callback<List<ClasseWithAttendance>>() {
                    @Override
                    public void onSuccess(List<ClasseWithAttendance> rows) {
                        renderProgress(rows);
                    }

                    @Override
                    public void onError(Exception e) {
                        renderProgress(java.util.Collections.emptyList());
                    }
                });
    }

    private void renderProgress(List<ClasseWithAttendance> rows) {
        stopSkeletonProgress();
        progressContainer.removeAllViews();
        if (rows.isEmpty()) {
            tvProgressEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvProgressEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < rows.size(); i++) {
            ClasseWithAttendance row = rows.get(i);
            View item = inflater.inflate(R.layout.item_attendance_row, progressContainer, false);
            if (i > 0) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
                lp.topMargin = (int) (20 * getResources().getDisplayMetrics().density);
                item.setLayoutParams(lp);
            }
            ((TextView) item.findViewById(R.id.tv_subject)).setText(row.getClasse().getNom());
            TextView tvValue = item.findViewById(R.id.tv_value);
            android.widget.ProgressBar progress = item.findViewById(R.id.progress);
            if (row.getNbSessionsFermees() == 0) {
                tvValue.setText(R.string.student_progress_no_session);
                progress.setProgress(100);
            } else {
                tvValue.setText(getString(R.string.student_progress_value_format, row.getTauxPresence()));
                progress.setProgress(row.getTauxPresence());
            }
            progressContainer.addView(item);
        }
    }

    private void loadGreeting() {
        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                tvGreeting.setText(getString(R.string.student_greeting_format, user.getNom()));
            }

            @Override
            public void onError(Exception e) { /* keep default */ }
        });
    }

    private void loadTodaySchedule() {
        String userId = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListTodayCoursesUseCase().execute(userId, UserRole.ETUDIANT,
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
        stopSkeletonToday();
        todayContainer.removeAllViews();
        if (classes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        long nowMin = minutesSinceMidnight();
        for (Classe c : classes) {
            MaterialCardView card = (MaterialCardView) inflater.inflate(
                    R.layout.item_today_course_student, todayContainer, false);
            ((TextView) card.findViewById(R.id.tv_course_name)).setText(c.getNom());
            ((TextView) card.findViewById(R.id.tv_course_time))
                    .setText(c.getHeureDebut() + " - " + c.getHeureFin());
            ((TextView) card.findViewById(R.id.tv_course_room)).setText(c.getSalle());

            TextView status = card.findViewById(R.id.tv_course_status);
            String label = computeStatus(c, nowMin);
            if (label != null) {
                status.setText(label);
                status.setVisibility(View.VISIBLE);
            }
            todayContainer.addView(card);
        }
    }

    /** "Ongoing" si dans la plage, "Upcoming" si commence plus tard, null si terminé. */
    private String computeStatus(Classe c, long nowMin) {
        long start = parseMinutes(c.getHeureDebut());
        long end = parseMinutes(c.getHeureFin());
        if (start < 0 || end < 0) return null;
        if (nowMin >= start && nowMin <= end) return "Ongoing";
        if (nowMin < start) return "Upcoming";
        return null;
    }

    private long parseMinutes(String hhmm) {
        if (hhmm == null || hhmm.length() < 4) return -1;
        String[] parts = hhmm.split(":");
        if (parts.length != 2) return -1;
        try {
            return Integer.parseInt(parts[0]) * 60L + Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long minutesSinceMidnight() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) * 60L + cal.get(Calendar.MINUTE);
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
            Toast.makeText(this, R.string.student_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
