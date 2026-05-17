package com.example.attendble.ui.student.profil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.example.attendble.domain.model.ClasseWithAttendance;
import com.example.attendble.domain.model.Etudiant;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.auth.LoginActivity;
import com.example.attendble.ui.common.Skeleton;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;
import com.example.attendble.ui.student.scan.SearchingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

/**
 * Écran profil de l'étudiant : nom + numEtud, chips (total/absences/taux),
 * cercle de présence global, 4 premières matières avec leur taux.
 */
public class ProfilActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvId;
    private TextView tvChipTotal;
    private TextView tvChipAbsences;
    private TextView tvChipAttendance;
    private TextView tvOverallValue;
    private CircularProgressIndicator progressOverall;
    private View skeletonSubjects;
    private View subjectsContainer;
    private View skeletonProfileTop;
    private View profileTopReal;
    private ObjectAnimator subjectsPulse;
    private ObjectAnimator topPulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_profil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tv_profile_name);
        tvId = findViewById(R.id.tv_profile_id);
        tvChipTotal = findViewById(R.id.tv_chip_total);
        tvChipAbsences = findViewById(R.id.tv_chip_absences);
        tvChipAttendance = findViewById(R.id.tv_chip_attendance);
        tvOverallValue = findViewById(R.id.tv_overall_value);
        progressOverall = findViewById(R.id.progress_overall);
        skeletonSubjects = findViewById(R.id.skeleton_subjects);
        subjectsContainer = findViewById(R.id.subjects_container);
        skeletonProfileTop = findViewById(R.id.skeleton_profile_top);
        profileTopReal = findViewById(R.id.profile_top_real);

        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            ServiceLocator.getAuthRepository().logout();
            Toast.makeText(this, R.string.sp_toast_logout, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bindBottomNav();
        loadProfile();
    }

    private void loadProfile() {
        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                tvName.setText(user.getNom() != null ? user.getNom() : "");
                if (user instanceof Etudiant) {
                    Etudiant e = (Etudiant) user;
                    if (e.getNumEtud() != null) {
                        tvId.setText(getString(R.string.sp_id_format, e.getNumEtud()));
                    }
                }
                loadClassesStats();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfilActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassesStats() {
        Skeleton.stop(subjectsPulse);
        Skeleton.stop(topPulse);
        subjectsPulse = Skeleton.pulse(skeletonSubjects);
        topPulse = Skeleton.pulse(skeletonProfileTop);
        skeletonSubjects.setVisibility(View.VISIBLE);
        skeletonProfileTop.setVisibility(View.VISIBLE);
        subjectsContainer.setVisibility(View.GONE);
        profileTopReal.setVisibility(View.GONE);
        String uid = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListClassesByEtudiantWithStatsUseCase().execute(uid,
                new Callback<List<ClasseWithAttendance>>() {
                    @Override
                    public void onSuccess(List<ClasseWithAttendance> classes) {
                        stopTopAndSubjectsSkeletons();
                        applyChipsAndOverall(classes);
                        applySubjects(classes);
                    }

                    @Override
                    public void onError(Exception e) {
                        stopTopAndSubjectsSkeletons();
                    }
                });
    }

    private void stopTopAndSubjectsSkeletons() {
        Skeleton.stop(subjectsPulse);
        Skeleton.stop(topPulse);
        skeletonSubjects.setVisibility(View.GONE);
        skeletonProfileTop.setVisibility(View.GONE);
        subjectsContainer.setVisibility(View.VISIBLE);
        profileTopReal.setVisibility(View.VISIBLE);
    }

    private void applyChipsAndOverall(List<ClasseWithAttendance> classes) {
        long totalSessions = 0;
        long totalPresent = 0;
        for (ClasseWithAttendance c : classes) {
            int sessions = c.getNbSessionsFermees();
            totalSessions += sessions;
            // tauxPresence% × sessions ≈ pointages PRESENT de l'étudiant
            totalPresent += Math.round(sessions * c.getTauxPresence() / 100f);
        }
        long absences = Math.max(0L, totalSessions - totalPresent);
        int avg = totalSessions == 0 ? 100 : Math.round(100f * totalPresent / totalSessions);

        tvChipTotal.setText(getString(R.string.sp_chip_total_format, totalSessions));
        tvChipAbsences.setText(getString(R.string.sp_chip_absences_format, absences));
        tvChipAttendance.setText(getString(R.string.sp_chip_attendance_format, avg));
        tvOverallValue.setText(avg + "%");
        progressOverall.setProgress(avg);
    }

    private void applySubjects(List<ClasseWithAttendance> classes) {
        int[][] ids = new int[][]{
                {R.id.tv_subject1_name, R.id.tv_subject1_meta, R.id.tv_subject1_value, R.id.progress_subject1},
                {R.id.tv_subject2_name, R.id.tv_subject2_meta, R.id.tv_subject2_value, R.id.progress_subject2},
                {R.id.tv_subject3_name, R.id.tv_subject3_meta, R.id.tv_subject3_value, R.id.progress_subject3},
                {R.id.tv_subject4_name, R.id.tv_subject4_meta, R.id.tv_subject4_value, R.id.progress_subject4},
        };
        for (int i = 0; i < ids.length; i++) {
            TextView name = findViewById(ids[i][0]);
            TextView meta = findViewById(ids[i][1]);
            TextView value = findViewById(ids[i][2]);
            ProgressBar bar = findViewById(ids[i][3]);
            if (i < classes.size()) {
                ClasseWithAttendance c = classes.get(i);
                int sessions = c.getNbSessionsFermees();
                int present = Math.round(sessions * c.getTauxPresence() / 100f);
                name.setText(c.getClasse().getNom());
                meta.setText(getString(R.string.sp_subject_meta_format, sessions, present));
                value.setText(c.getTauxPresence() + "%");
                bar.setProgress(c.getTauxPresence());
            } else {
                name.setText("—");
                meta.setText("");
                value.setText("");
                bar.setProgress(0);
            }
        }
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_profile);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                return true;
            }
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
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
            Toast.makeText(this, R.string.student_toast_nav, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
