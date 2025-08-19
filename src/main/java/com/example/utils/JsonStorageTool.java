package com.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class FilePersistence<T> {
    Dotenv dotenv = Dotenv.load();
    private final String FOLDER_PATH = dotenv.get("FOLDER_DATA_LOCATION");

    private final Path filePath;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<T>> typeReference;

    private List<T> items;

    public FilePersistence(String fileName, TypeReference<List<T>> typeReference, List<T> items) {
        objectMapper.registerModule(new JavaTimeModule());
        this.filePath = Path.of(FOLDER_PATH, fileName);
        this.typeReference = typeReference;
        this.items = items;
    }

    protected void loadFromFile() {
        File file = filePath.toFile();
        if (file.exists() && file.length() > 0) {
            try {
                List<T> loaded = objectMapper.readValue(file, typeReference);
                this.items.clear();
                this.items.addAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
                this.items = new ArrayList<>();
            }
        } else {
            this.items = new ArrayList<>();
            saveToFile();
        }
    }

    protected void saveToFile() {
        try {
            objectMapper.writeValue(filePath.toFile(), this.items);
            System.out.println("items: " + this.items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class JsonStorageTool<T> {
    // syncronized CRUD operations for multi acces - prevent race conditions
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private List<T> items = new ArrayList<>();
    private FilePersistence<T> filePersistence;

    public JsonStorageTool(String fileName, TypeReference<List<T>> typeReference) {
        if (fileName == null || typeReference == null) {
            System.err.println("❌ Critical Error: Invalid arguments provided.");
            Thread.dumpStack();
            System.exit(1);
        }

        filePersistence = new FilePersistence(fileName, typeReference, items);
        filePersistence.loadFromFile();
    }

    public List<T> getItems() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(this.items);
        } catch (Exception e) {
            System.err.println("Failed to get items: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void addItem(T item) {
        rwLock.writeLock().lock();
        try {
            this.items.add(item);
            filePersistence.saveToFile();
        } catch (Exception e) {
            System.err.println("Failed to add item: " + e.getMessage());
            e.printStackTrace();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void removeItem(T item) {
        rwLock.writeLock().lock();
        try {
            if (this.items.contains(item)) {
                this.items.remove(item);
                filePersistence.saveToFile();
            }
        } catch (Exception e) {
            System.err.println("Failed to remove item: " + e.getMessage());
            e.printStackTrace();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void updateItem(T item, T updatedItem) {
        rwLock.writeLock().lock();
        try {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(item)) {
                    this.items.set(i, updatedItem);
                    break;
                }
            }
            filePersistence.saveToFile();
        } catch (Exception e) {
            System.err.println("Failed to update item: " + e.getMessage());
            e.printStackTrace();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
