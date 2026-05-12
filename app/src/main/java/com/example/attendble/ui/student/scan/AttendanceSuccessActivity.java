package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;

/**
 * Écran de confirmation après pointage réussi (BLE + code validés).
 * À recevoir le sessionId/heure réels via Intent quand la couche métier sera branchée.
 */
public class AttendanceSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btn_home).setOnClickListener(v -> goTo(HomeActivity.class));
        findViewById(R.id.btn_classes).setOnClickListener(v -> goTo(MyClassesActivity.class));
    }

    private void goTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
