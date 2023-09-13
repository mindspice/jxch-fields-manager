package io.mindspice.jxch.fields.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class CircularQueue<T> {
    private final ArrayList<T> buffer;
    private final int capacity;
    private int head;
    private int tail;
    private int size;

    public CircularQueue(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized boolean isFull() {
        return size == capacity;
    }

    public synchronized List<T> ListView() {
        return Collections.unmodifiableList(buffer);
    }

    public synchronized void add(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot add null to the queue");
        }
        if (isFull()) { return; }

        if (size < capacity) {
            buffer.add(element);
        } else {
            buffer.set(tail, element);
        }

        tail = (tail + 1) % capacity;
        size++;
    }

    public synchronized Optional<T> poll() {
        if (isEmpty()) {
            return Optional.empty();
        }
        T element = buffer.get(head);
        head = (head + 1) % capacity;
        size--;
        return Optional.of(element);
    }

    public synchronized Optional<T> peek() {
        if (isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(buffer.get(head));
    }

    public synchronized void poll(Consumer<T> consumer) {
        poll().ifPresent(consumer);
    }

    public synchronized void peek(Consumer<T> consumer) {
        peek().ifPresent(consumer);
    }
}

