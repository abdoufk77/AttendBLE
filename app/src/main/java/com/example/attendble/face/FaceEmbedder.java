package com.example.attendble.face;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Inférence MobileFaceNet (TensorFlow Lite) : Bitmap visage cropé → embedding float[192].
 * Modèle attendu : assets/mobilefacenet.tflite (input 1×112×112×3 normalisé [-1, 1]).
 */
public class FaceEmbedder {

    private static final String MODEL_ASSET = "mobilefacenet.tflite";
    private static final int INPUT_SIZE = 112;
    private static final int EMBEDDING_SIZE = 192;
    private static final int CHANNELS = 3;

    private final Interpreter interpreter;
    private final ByteBuffer inputBuffer;

    public FaceEmbedder(@NonNull Context context) throws IOException {
        MappedByteBuffer model = loadModel(context);
        Interpreter.Options opts = new Interpreter.Options();
        opts.setNumThreads(2);
        this.interpreter = new Interpreter(model, opts);

        this.inputBuffer = ByteBuffer
                .allocateDirect(INPUT_SIZE * INPUT_SIZE * CHANNELS * 4)
                .order(ByteOrder.nativeOrder());
    }

    /** Crope le visage selon {@code faceBounds} dans {@code fullFrame}, puis extrait l'embedding. */
    public float[] embed(@NonNull Bitmap fullFrame, @NonNull Rect faceBounds) {
        return embed(safeCrop(fullFrame, faceBounds));
    }

    /** Extrait l'embedding directement à partir d'un bitmap visage déjà cropé. */
    public synchronized float[] embed(@NonNull Bitmap faceCrop) {
        Bitmap resized = Bitmap.createScaledBitmap(faceCrop, INPUT_SIZE, INPUT_SIZE, true);
        fillInputBuffer(resized);
        if (resized != faceCrop) resized.recycle();

        float[][] output = new float[1][EMBEDDING_SIZE];
        interpreter.run(inputBuffer, output);
        return l2Normalize(output[0]);
    }

    /** Convertit ARGB_8888 → float [-1, 1] dans le buffer d'entrée. */
    private void fillInputBuffer(Bitmap bmp) {
        inputBuffer.rewind();
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        for (int p : pixels) {
            float r = ((p >> 16) & 0xFF);
            float g = ((p >> 8) & 0xFF);
            float b = (p & 0xFF);
            inputBuffer.putFloat((r - 127.5f) / 128f);
            inputBuffer.putFloat((g - 127.5f) / 128f);
            inputBuffer.putFloat((b - 127.5f) / 128f);
        }
    }

    private static Bitmap safeCrop(Bitmap src, Rect r) {
        int left = Math.max(0, r.left);
        int top = Math.max(0, r.top);
        int right = Math.min(src.getWidth(), r.right);
        int bottom = Math.min(src.getHeight(), r.bottom);
        int w = Math.max(1, right - left);
        int h = Math.max(1, bottom - top);
        return Bitmap.createBitmap(src, left, top, w, h);
    }

    private static float[] l2Normalize(float[] v) {
        double sum = 0;
        for (float x : v) sum += x * x;
        float norm = (float) Math.sqrt(Math.max(sum, 1e-10));
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) out[i] = v[i] / norm;
        return out;
    }

    private static MappedByteBuffer loadModel(Context ctx) throws IOException {
        AssetFileDescriptor fd = ctx.getAssets().openFd(MODEL_ASSET);
        try (FileInputStream is = new FileInputStream(fd.getFileDescriptor());
             FileChannel channel = is.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
        } finally {
            fd.close();
        }
    }

    public void close() {
        interpreter.close();
    }

    public static int embeddingSize() {
        return EMBEDDING_SIZE;
    }
}
