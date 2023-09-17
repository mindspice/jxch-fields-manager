package io.mindspice.jxch.fields.data.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ConcurrentCircularBuffer<T> implements Iterable<T> {
    private final List<T> buffer;
    private final int capacity;
    private int head;
    private int size;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ConcurrentCircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
        this.head = 0;
        this.size = 0;
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return size == 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isFull() {
        lock.readLock().lock();
        try {
            return size == capacity;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void add(T value) {
        lock.writeLock().lock();
        try {
            if (isFull()) {
                buffer.set(head, value);
                head = (head + 1) % capacity;
            } else {
                buffer.add(value);
                size++;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<T> getAsList() {
        lock.readLock().lock();
        try {
            List<T> rntList = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                int actualIndex = (head + i) % capacity;
                rntList.add(buffer.get(actualIndex));
            }
            return rntList;
        } finally {
            lock.readLock().unlock();
        }
    }

    public T getFromOldest(int index) {
        lock.readLock().lock();
        try {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }
            int actualIndex = (head + index) % capacity;
            return buffer.get(actualIndex);
        } finally {
            lock.readLock().unlock();
        }
    }

    public T getFromNewest(int index) {
        lock.readLock().lock();
        try {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        int actualIndex = (head + size - 1 - index) % capacity;
        return buffer.get(actualIndex);
        } finally {
            lock.readLock().unlock();
        }
    }

    public T takeHead() {
        lock.writeLock().lock();
        try {
            if (isEmpty()) {
                return null; // or throw an exception
            }
            T value = buffer.get(head);
            head = (head + 1) % capacity;
            size--;
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T peekHead() {
        lock.readLock().lock();
        try {
            if (isEmpty()) {
                return null; // or throw an exception
            }
            return buffer.get(head);
        } finally {
            lock.readLock().unlock();
        }
    }

    public T takeTail() {
        lock.writeLock().lock();
        try {
            if (isEmpty()) {
                return null; // or throw an exception
            }
            int tail = (head + size - 1) % capacity;
            T value = buffer.get(tail);
            size--;
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T peekTail() {
        lock.readLock().lock();
        try {
            if (isEmpty()) {
                return null; // or throw an exception
            }
            int tail = (head + size - 1) % capacity;
            return buffer.get(tail);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ConcurrentCircularBufferIterator();
    }

    public class ConcurrentCircularBufferIterator implements Iterator<T> {
        private final int initialHead;
        private final int initialSize;
        private final List<T> bufferView;
        private int currentIdx;
        private int traversed;


        public ConcurrentCircularBufferIterator() {
            lock.readLock().lock();
            try {
                this.initialHead = head;
                this.initialSize = size;
                this.bufferView = buffer;
                this.currentIdx = initialHead;
            } finally {
                lock.readLock().unlock();
            }
            this.traversed = 0;
        }

        @Override
        public boolean hasNext() {
            return traversed < initialSize;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T value = bufferView.get(currentIdx);
            currentIdx = (currentIdx + 1) % capacity;
            traversed++;
            return value;
        }
    }


    @Override
    public String toString() { return buffer.toString(); }


}


