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
import com.example.attendble.ui.prof.classes.MyClassesActivity;
import com.example.attendble.ui.prof.home.HomeActivity;
import com.example.attendble.ui.prof.profil.ProfilActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Écran "Serve" : le prof choisit une classe pour lancer une session BLE (Advertiser).
 * Le tap "Start" ouvrira plus tard ActiveSessionActivity avec code 4-chiffres + timer.
 */
public class ServeHomeActivity extends AppCompatActivity {

    private static class ClassStub {
        final int name, code, room, students;

        ClassStub(int name, int code, int room, int students) {
            this.name = name;
            this.code = code;
            this.room = room;
            this.students = students;
        }
    }

    private final ClassStub[] stubClasses = new ClassStub[]{
            new ClassStub(R.string.serve_course1_name, R.string.serve_course1_code,
                    R.string.serve_course1_room, R.string.serve_course1_students),
            new ClassStub(R.string.serve_course2_name, R.string.serve_course2_code,
                    R.string.serve_course2_room, R.string.serve_course2_students)
    };

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

        populateClasses();
        bindBottomNav();
    }

    private void populateClasses() {
        LinearLayout container = findViewById(R.id.classes_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ClassStub clazz : stubClasses) {
            View card = inflater.inflate(R.layout.item_serve_class, container, false);
            ((TextView) card.findViewById(R.id.tv_class_name)).setText(clazz.name);
            ((TextView) card.findViewById(R.id.tv_class_code)).setText(clazz.code);
            ((TextView) card.findViewById(R.id.tv_class_room)).setText(clazz.room);
            ((TextView) card.findViewById(R.id.tv_class_students)).setText(clazz.students);

            MaterialButton btnStart = card.findViewById(R.id.btn_start);
            btnStart.setOnClickListener(v -> startActivity(new Intent(this, ActiveSessionActivity.class)));
            container.addView(card);
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
