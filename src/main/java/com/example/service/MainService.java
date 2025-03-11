package com.example.service;

import java.util.List;
import java.util.UUID;

public abstract class MainService<T> {
    public abstract List<T> getAll();
    public abstract T getById(UUID id);
    public abstract T add(T entity);
    public abstract void deleteById(UUID id);
}