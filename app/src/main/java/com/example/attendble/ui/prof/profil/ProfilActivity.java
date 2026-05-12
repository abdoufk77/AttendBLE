package com.example.attendble.ui.prof.profil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.data.ServiceLocator;
import com.example.attendble.ui.auth.LoginActivity;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.example.attendble.ui.prof.serve.ServeHomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Écran profil du professeur : avatar, stats, cours gérés, settings, logout.
 * Les listeners sont des stubs — la logique métier sera branchée via ServiceLocator (use cases).
 */
public class ProfilActivity extends AppCompatActivity {

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

        bindCourseCards();
        bindSettingsRows();
        bindLogout();
        bindBottomNav();
    }

    private void bindCourseCards() {
        View card1 = findViewById(R.id.card_course_1);
        View card2 = findViewById(R.id.card_course_2);
        View.OnClickListener courseListener = v -> toast(R.string.prof_toast_course_clicked);
        card1.setOnClickListener(courseListener);
        card2.setOnClickListener(courseListener);
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
