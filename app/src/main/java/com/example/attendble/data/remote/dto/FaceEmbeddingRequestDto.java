package com.example.attendble.data.remote.dto;

public class FaceEmbeddingRequestDto {
    public float[] embedding;

    public FaceEmbeddingRequestDto(float[] embedding) {
        this.embedding = embedding;
    }
}
