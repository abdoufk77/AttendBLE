package com.example.attendble.ui.student.scan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ble.StudentBroadcaster;
import com.example.attendble.ui.student.classes.MyClassesActivity;
import com.example.attendble.ui.student.home.HomeActivity;

/**
 * Confirmation pointage. Promote le broadcast BLE déjà en cours (lancé en mode PENDING
 * dans SessionCodeActivity) vers le statut CONFIRMED — le prof bascule alors la ligne
 * de l'étudiant de gris → vert.
 */
public class AttendanceSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_BEACON_UUID = "extra_beacon_uuid";
    private static final String TAG = "AttendBLE/Response";

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

        Log.i(TAG, "Promote broadcast PENDING → CONFIRMED");
        StudentBroadcaster.get().promoteToConfirmed(getApplicationContext());
    }

    private void goTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
