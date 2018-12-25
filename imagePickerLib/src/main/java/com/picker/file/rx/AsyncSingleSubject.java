package com.picker.file.rx;

import io.reactivex.annotations.Nullable;
import io.reactivex.annotations.NonNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observer;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.DeferredScalarDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.AsyncSubject;

/**
 * AsyncSingleSubject is full copy of the {@link AsyncSubject} except the case, that cached value
 * exist only till the first subscribe.
 */
public final class AsyncSingleSubject<T> extends Subject<T> {

    @SuppressWarnings("rawtypes")
    static final AsyncDisposable[] EMPTY = new AsyncDisposable[0];

    @SuppressWarnings("rawtypes")
    static final AsyncDisposable[] TERMINATED = new AsyncDisposable[0];

    final AtomicReference<AsyncDisposable<T>[]> subscribers;

    /**
     * Write before updating subscribers, read after reading subscribers as TERMINATED.
     */
    Throwable error;

    /**
     * Write before updating subscribers, read after reading subscribers as TERMINATED.
     */
    T value;

    /**
     * Creates a new AsyncProcessor.
     *
     * @param <T> the value type to be received and emitted
     * @return the new AsyncProcessor instance
     */
    @CheckReturnValue
    @NonNull
    public static <T> AsyncSingleSubject<T> create() {
        return new AsyncSingleSubject<T>();
    }

    /**
     * Constructs an AsyncSubject.
     *
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    AsyncSingleSubject() {
        this.subscribers = new AtomicReference<AsyncDisposable<T>[]>(EMPTY);
    }

    @Override
    public void onSubscribe(Disposable s) {
        if (subscribers.get() == TERMINATED) {
            s.dispose();
        }
    }

    @Override
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (subscribers.get() == TERMINATED) {
            return;
        }
        value = t;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable t) {
        ObjectHelper.requireNonNull(t, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        value = null;
        error = t;
        for (AsyncDisposable<T> as : subscribers.getAndSet(TERMINATED)) {
            as.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        T v = value;
        AsyncDisposable<T>[] array = subscribers.getAndSet(TERMINATED);
        if (v == null) {
            for (AsyncDisposable<T> as : array) {
                as.onComplete();
            }
        } else {
            for (AsyncDisposable<T> as : array) {
                as.complete(v);
            }
        }
    }

    @Override
    public boolean hasObservers() {
        return subscribers.get().length != 0;
    }

    @Override
    public boolean hasThrowable() {
        return subscribers.get() == TERMINATED && error != null;
    }

    @Override
    public boolean hasComplete() {
        return subscribers.get() == TERMINATED && error == null;
    }

    @Override
    public Throwable getThrowable() {
        return subscribers.get() == TERMINATED ? error : null;
    }

    @Override
    protected void subscribeActual(Observer<? super T> s) {
        AsyncDisposable<T> as = new AsyncDisposable<T>(s, this);
        s.onSubscribe(as);
        if (add(as)) {
            if (as.isDisposed()) {
                remove(as);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                s.onError(ex);
            } else {
                T v = value;
                if (v != null) {
                    as.complete(v);
                } else {
                    as.onComplete();
                }
                value = null;
            }
        }
    }

    /**
     * Tries to add the given subscriber to the subscribers array atomically
     * or returns false if the subject has terminated.
     *
     * @param ps the subscriber to add
     * @return true if successful, false if the subject has terminated
     */
    boolean add(AsyncDisposable<T> ps) {
        for (; ; ) {
            AsyncDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            AsyncDisposable<T>[] b = new AsyncDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     *
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(AsyncDisposable<T> ps) {
        for (; ; ) {
            AsyncDisposable<T>[] a = subscribers.get();
            int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            AsyncDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new AsyncDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * Returns true if the subject has any value.
     * <p>The method is thread-safe.
     *
     * @return true if the subject has any value
     */
    public boolean hasValue() {
        return subscribers.get() == TERMINATED && value != null;
    }

    /**
     * Returns a single value the Subject currently has or null if no such value exists.
     * <p>The method is thread-safe.
     *
     * @return a single value the Subject currently has or null if no such value exists
     */
    @Nullable
    public T getValue() {
        return subscribers.get() == TERMINATED ? value : null;
    }

    /**
     * Returns an Object array containing snapshot all values of the Subject.
     * <p>The method is thread-safe.
     *
     * @return the array containing the snapshot of all values of the Subject
     * @deprecated in 2.1.14; put the result of {@link #getValue()} into an array manually, will be removed in 3.x
     */
    @Deprecated
    public Object[] getValues() {
        T v = getValue();
        return v != null ? new Object[]{v} : new Object[0];
    }

    /**
     * Returns a typed array containing a snapshot of all values of the Subject.
     * <p>The method follows the conventions of Collection.toArray by setting the array element
     * after the last value to null (if the capacity permits).
     * <p>The method is thread-safe.
     *
     * @param array the target array to copy values into if it fits
     * @return the given array if the values fit into it or a new array containing all values
     * @deprecated in 2.1.14; put the result of {@link #getValue()} into an array manually, will be removed in 3.x
     */
    @Deprecated
    public T[] getValues(T[] array) {
        T v = getValue();
        if (v == null) {
            if (array.length != 0) {
                array[0] = null;
            }
            return array;
        }
        if (array.length == 0) {
            array = Arrays.copyOf(array, 1);
        }
        array[0] = v;
        if (array.length != 1) {
            array[1] = null;
        }
        return array;
    }

    static final class AsyncDisposable<T> extends DeferredScalarDisposable<T> {
        private static final long serialVersionUID = 5629876084736248016L;

        final AsyncSingleSubject<T> parent;

        AsyncDisposable(Observer<? super T> actual, AsyncSingleSubject<T> parent) {
            super(actual);
            this.parent = parent;
        }

        @Override
        public void dispose() {
            if (super.tryDispose()) {
                parent.remove(this);
            }
        }

        void onComplete() {
            if (!isDisposed()) {
                actual.onComplete();
            }
        }

        void onError(Throwable t) {
            if (isDisposed()) {
                RxJavaPlugins.onError(t);
            } else {
                actual.onError(t);
            }
        }
    }
}
