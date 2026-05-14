package com.example.attendble.face;

import java.util.List;

/**
 * Outils de comparaison d'embeddings : similarité cosinus + agrégation L2-normalisée
 * de plusieurs captures en un seul template.
 */
public final class FaceMatcher {

    /** Seuil par défaut au-delà duquel on considère deux embeddings comme provenant de la même personne. */
    public static final float DEFAULT_THRESHOLD = 0.70f;

    private FaceMatcher() {}

    /** Cosinus de l'angle entre {@code a} et {@code b}. Pour des vecteurs L2-normalisés c'est simplement le produit scalaire. */
    public static float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Embedding length mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        double denom = Math.sqrt(Math.max(na, 1e-10)) * Math.sqrt(Math.max(nb, 1e-10));
        return (float) (dot / denom);
    }

    public static boolean matches(float[] a, float[] b) {
        return matches(a, b, DEFAULT_THRESHOLD);
    }

    public static boolean matches(float[] a, float[] b, float threshold) {
        return cosineSimilarity(a, b) >= threshold;
    }

    /**
     * Construit le template final à partir des N captures d'enrôlement :
     * 1) chaque embedding est L2-normalisé (déjà fait par FaceEmbedder)
     * 2) moyenne composante par composante
     * 3) re-normalisation L2 du résultat
     */
    public static float[] averageL2(List<float[]> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalArgumentException("Empty embeddings list");
        }
        int dim = embeddings.get(0).length;
        float[] avg = new float[dim];
        for (float[] e : embeddings) {
            if (e.length != dim) throw new IllegalArgumentException("Inconsistent embedding sizes");
            for (int i = 0; i < dim; i++) avg[i] += e[i];
        }
        float n = embeddings.size();
        for (int i = 0; i < dim; i++) avg[i] /= n;

        double sum = 0;
        for (float v : avg) sum += v * v;
        float norm = (float) Math.sqrt(Math.max(sum, 1e-10));
        for (int i = 0; i < dim; i++) avg[i] /= norm;
        return avg;
    }
}
