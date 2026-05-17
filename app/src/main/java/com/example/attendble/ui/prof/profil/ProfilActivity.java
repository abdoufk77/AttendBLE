package com.example.attendble.ui.prof.profil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.attendble.domain.model.Professeur;
import com.example.attendble.domain.model.ProfStats;
import com.example.attendble.domain.model.User;
import com.example.attendble.ui.auth.LoginActivity;
import com.example.attendble.ui.prof.classes.ClassDetailsActivity;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.example.attendble.ui.prof.serve.ServeHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Écran profil du professeur : avatar, stats (classes/étudiants/présence),
 * 2 premières classes managées, settings stubs, logout.
 */
public class ProfilActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvDepartment;
    private TextView tvStatsClasses;
    private TextView tvStatsStudents;
    private TextView tvStatsAttendance;
    private MaterialCardView card1;
    private MaterialCardView card2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prof_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tv_profile_name);
        tvDepartment = findViewById(R.id.tv_profile_department);
        tvStatsClasses = findViewById(R.id.tv_stats_classes_value);
        tvStatsStudents = findViewById(R.id.tv_stats_students_value);
        tvStatsAttendance = findViewById(R.id.tv_stats_attendance_value);
        card1 = findViewById(R.id.card_course_1);
        card2 = findViewById(R.id.card_course_2);

        bindSettingsRows();
        bindLogout();
        bindBottomNav();
        loadProfile();
    }

    private void loadProfile() {
        ServiceLocator.getAuthRepository().getCurrentUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                tvName.setText(user.getNom() != null ? user.getNom() : "");
                if (user instanceof Professeur) {
                    Professeur p = (Professeur) user;
                    tvDepartment.setText(p.getDepartment() != null ? p.getDepartment() : "");
                }
                loadStats();
                loadCourses();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfilActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStats() {
        String uid = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideGetProfStatsUseCase().execute(uid, new Callback<ProfStats>() {
            @Override
            public void onSuccess(ProfStats s) {
                tvStatsClasses.setText(String.valueOf(s.getTotalClasses()));
                tvStatsStudents.setText(String.valueOf(s.getTotalStudents()));
                tvStatsAttendance.setText(s.getAvgAttendance() + "%");
            }

            @Override
            public void onError(Exception e) { /* silencieux */ }
        });
    }

    private void loadCourses() {
        String uid = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListClassesByProfWithStatsUseCase().execute(uid,
                new Callback<List<ClasseWithAttendance>>() {
                    @Override
                    public void onSuccess(List<ClasseWithAttendance> classes) {
                        bindCourseCard(card1, R.id.tv_course1_name, R.id.tv_course1_code,
                                R.id.tv_course1_room, R.id.tv_course1_rate,
                                classes.size() > 0 ? classes.get(0) : null);
                        bindCourseCard(card2, R.id.tv_course2_name, R.id.tv_course2_code,
                                R.id.tv_course2_room, R.id.tv_course2_rate,
                                classes.size() > 1 ? classes.get(1) : null);
                    }

                    @Override
                    public void onError(Exception e) { /* silencieux */ }
                });
    }

    private void bindCourseCard(MaterialCardView card, int nameId, int codeId, int roomId, int rateId,
                                ClasseWithAttendance c) {
        if (c == null) {
            card.setVisibility(View.GONE);
            return;
        }
        card.setVisibility(View.VISIBLE);
        ((TextView) card.findViewById(nameId)).setText(c.getClasse().getNom());
        ((TextView) card.findViewById(codeId)).setText(c.getClasse().getCodeInvitation());
        String room = c.getClasse().getSalle();
        ((TextView) card.findViewById(roomId)).setText(room == null ? "" : room);
        ((TextView) card.findViewById(rateId)).setText(c.getTauxPresence() + "%");
        String classeId = c.getClasse().getClasseId();
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassDetailsActivity.class);
            intent.putExtra("classeId", classeId);
            startActivity(intent);
        });
    }

    private void bindSettingsRows() {
        View.OnClickListener settingsListener = v -> toast(R.string.prof_toast_settings_clicked);
        findViewById(R.id.row_settings_account).setOnClickListener(settingsListener);
        findViewById(R.id.row_settings_notifications).setOnClickListener(settingsListener);
        findViewById(R.id.row_settings_privacy).setOnClickListener(settingsListener);
    }

    private void bindLogout() {
        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            ServiceLocator.getAuthRepository().logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_profile);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
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
            if (id == R.id.nav_serve) {
                startActivity(new Intent(this, ServeHomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
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
