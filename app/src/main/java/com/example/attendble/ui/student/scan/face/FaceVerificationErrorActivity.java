package com.example.attendble.ui.student.scan.face;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.attendble.R;
import com.example.attendble.ui.student.home.HomeActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * État d'échec de la vérification visuelle : visage non reconnu ou liveness échoué.
 * "Try again" relance FaceVerificationActivity ; "Cancel" retourne à l'accueil étudiant.
 */
public class FaceVerificationErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_verification_error);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar topbar = findViewById(R.id.topbar);
        topbar.setNavigationOnClickListener(v -> finish());

        MaterialButton tryAgain = findViewById(R.id.btn_try_again);
        tryAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceVerificationActivity.class));
            finish();
        });

        MaterialButton cancel = findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
