package io.mindspice.jxch.fields.data.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class CircularBuffer<T> implements Iterable<T> {
    private final List<T> buffer;
    private final int capacity;
    private int head;
    private int size;

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
        this.head = 0;
        this.size = 0;
    }

    public boolean isEmpty() { return size == 0; }

    public boolean isFull() { return size == capacity; }

    public int size() { return size; }

    public void add(T value) {
        if (isFull()) {
            buffer.set(head, value);
            head = (head + 1) % capacity;
        } else {
            buffer.add(value);
            size++;
        }
    }

    public List<T> getAsList() {
        List<T> rntList = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            rntList.add(getFromNewest(i));
        }
        return rntList;
    }

    public T getFromOldest(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        int actualIndex = (head + index) % capacity;
        return buffer.get(actualIndex);
    }

    public T getFromNewest(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        int actualIndex = (head + size - 1 - index) % capacity;
        return buffer.get(actualIndex);
    }

    @Override
    public Iterator<T> iterator() { return new RollOffArrayIterator(buffer, head, size); }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private class RollOffArrayIterator implements Iterator<T> {
        private final List<T> buffer;
        private final int currentHead;
        private final int currentSize;
        private int currentIdx;
        private int traversed;

        public RollOffArrayIterator(List<T> buffer, int currentHead, int currentSize) {
            this.buffer = buffer;
            this.currentHead = currentHead;
            this.currentSize = currentSize;
            this.currentIdx = currentHead;
            this.traversed = 0;
        }

        @Override
        public boolean hasNext() {
            return traversed < currentSize;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T value = buffer.get(currentIdx);
            currentIdx = (currentIdx + 1) % capacity;
            traversed++;
            return value;
        }
    }

    @Override
    public String toString() { return buffer.toString(); }


}


