package com.example.attendble.domain;

/**
 * Callback générique pour opérations asynchrones (repositories, use cases).
 * Permet à la couche data (in-memory, plus tard Firebase) de notifier l'UI sans bloquer le thread.
 */
public interface Callback<T> {
    void onSuccess(T data);

    void onError(Exception e);
}