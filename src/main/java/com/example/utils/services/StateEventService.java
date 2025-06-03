package com.example.utils.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class StateEventService {
    private static final StateEventService instance = new StateEventService();

    private final Map<String, Set<Consumer<Object>>> listeners = new HashMap<>();
    private final Map<String, Object> lastEmittedValue = new HashMap<>();

    private StateEventService() {
    }

    public static StateEventService getInstance() {
        return instance;
    }

    public void subscribe(String eventName, Consumer<Object> callback) {
        listeners.computeIfAbsent(eventName, k -> new HashSet<>()).add(callback);

        if (lastEmittedValue.containsKey(eventName)) {
            callback.accept(lastEmittedValue.get(eventName));
        }
    }

    public void emit(String eventName, Object payload) {
        lastEmittedValue.put(eventName, payload);

        if (listeners.containsKey(eventName)) {
            for (Consumer<Object> listener : listeners.get(eventName)) {
                listener.accept(payload);
            }
        }
    }

    public void unsubscribe(String eventName, Consumer<Object> callback) {
        if (listeners.containsKey(eventName)) {
            listeners.get(eventName).remove(callback);
        }
    }
}
