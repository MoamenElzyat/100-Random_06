package com.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Repository;

@Repository
public abstract class MainRepository<T> {

    protected ObjectMapper objectMapper = new ObjectMapper();

    // Child classes must override these methods
    protected abstract String getDataPath();
    protected abstract Class<T[]> getArrayType();

    public ArrayList<T> findAll() {
        try {
            File file = new File(getDataPath());
            if (!file.exists()) {
                return new ArrayList<>();
            }
            T[] array = objectMapper.readValue(file, getArrayType());
            return new ArrayList<>(Arrays.asList(array));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON file: " + getDataPath(), e);
        }
    }

    public void saveAll(ArrayList<T> data) {
        try {
            objectMapper.writeValue(new File(getDataPath()), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to JSON file: " + getDataPath(), e);
        }
    }

    public void save(T data) {
        ArrayList<T> allData = findAll();
        allData.add(data);
        saveAll(allData);
    }
}