package com.example.attendble.ui.prof.serve;

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
import com.example.attendble.domain.enums.UserRole;
import com.example.attendble.domain.model.Classe;
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.example.attendble.ui.prof.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;

/**
 * Écran "Serve" : affiche uniquement les classes du prof programmées aujourd'hui.
 * Tap "Start" lance ActiveSessionActivity (BLE Advertiser + code 4 chiffres).
 */
public class ServeHomeActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_serve_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        container = findViewById(R.id.classes_container);
        tvEmpty = findViewById(R.id.tv_classes_empty);

        bindBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayClasses();
    }

    private void loadTodayClasses() {
        String profId = ServiceLocator.getAuthRepository().getCurrentUserId();
        ServiceLocator.provideListTodayCoursesUseCase().execute(profId, UserRole.PROFESSEUR,
                new Callback<List<Classe>>() {
                    @Override
                    public void onSuccess(List<Classe> classes) {
                        render(classes);
                    }

                    @Override
                    public void onError(Exception e) {
                        render(java.util.Collections.emptyList());
                    }
                });
    }

    private void render(List<Classe> classes) {
        container.removeAllViews();
        if (classes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Classe c : classes) {
            View card = inflater.inflate(R.layout.item_serve_class, container, false);
            ((TextView) card.findViewById(R.id.tv_class_name)).setText(c.getNom());
            ((TextView) card.findViewById(R.id.tv_class_code)).setText(c.getCodeInvitation());
            ((TextView) card.findViewById(R.id.tv_class_room)).setText(c.getSalle());
            ((TextView) card.findViewById(R.id.tv_class_students))
                    .setText(getString(R.string.serve_class_students_format, c.getNbEtudiants()));
            ((TextView) card.findViewById(R.id.tv_class_schedule))
                    .setText(formatSchedule(c.getHeureDebut(), c.getHeureFin()));

            final String classeId = c.getClasseId();
            final String hDebut = c.getHeureDebut();
            final String hFin = c.getHeureFin();
            MaterialButton btnStart = card.findViewById(R.id.btn_start);
            boolean inSchedule = isWithinSchedule(hDebut, hFin);
            // Visuellement grisé, mais cliquable pour montrer pourquoi (UX > setEnabled).
            card.setAlpha(inSchedule ? 1f : 0.5f);
            btnStart.setOnClickListener(v -> {
                if (inSchedule) {
                    Intent i = new Intent(this, ActiveSessionActivity.class);
                    i.putExtra("classeId", classeId);
                    startActivity(i);
                } else {
                    Calendar now = Calendar.getInstance();
                    String nowStr = String.format(java.util.Locale.US, "%02d:%02d",
                            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                    Toast.makeText(this,
                            "Hors créneau : maintenant " + nowStr
                                    + " · cours " + hDebut + "–" + hFin,
                            Toast.LENGTH_LONG).show();
                }
            });
            container.addView(card);
        }
    }

    private String formatSchedule(String hDebut, String hFin) {
        if (hDebut == null || hFin == null) return "";
        return hDebut + "–" + hFin;
    }

    // Compare l'heure courante à [heureDebut, heureFin] (format "HH:mm"). Si l'un est null
    // ou mal formé, on considère la classe disponible (fallback permissif).
    private boolean isWithinSchedule(String hDebut, String hFin) {
        Integer debut = parseMinutes(hDebut);
        Integer fin = parseMinutes(hFin);
        if (debut == null || fin == null) return true;
        Calendar now = Calendar.getInstance();
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        return nowMin >= debut && nowMin < fin;
    }

    private Integer parseMinutes(String hhmm) {
        if (hhmm == null || hhmm.length() < 4 || !hhmm.contains(":")) return null;
        try {
            String[] parts = hhmm.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private void bindBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_serve);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_serve) {
                return true;
            }
            if (id == R.id.nav_classes) {
                startActivity(new Intent(this, MyClassesActivity.class));
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
