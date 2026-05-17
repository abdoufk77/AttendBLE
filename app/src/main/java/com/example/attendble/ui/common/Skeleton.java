package com.example.attendble.ui.common;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Helper Material 3 : animation "pulse" sur la vue (alpha 0.3 ↔ 0.7 en boucle).
 * Convention : afficher un layout skeleton placeholder pendant le chargement réseau,
 * appeler {@link #pulse(View)}, puis {@link #stop(ObjectAnimator)} + masquer la vue
 * quand les vraies données arrivent.
 */
public final class Skeleton {

    private static final long DURATION_MS = 700L;
    private static final float ALPHA_MIN = 0.3f;
    private static final float ALPHA_MAX = 0.7f;

    private Skeleton() {
    }

    public static ObjectAnimator pulse(View view) {
        view.setAlpha(ALPHA_MIN);
        ObjectAnimator a = ObjectAnimator.ofFloat(view, "alpha", ALPHA_MIN, ALPHA_MAX);
        a.setDuration(DURATION_MS);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        a.start();
        return a;
    }

    public static void stop(ObjectAnimator animator) {
        if (animator != null) animator.cancel();
    }
}
