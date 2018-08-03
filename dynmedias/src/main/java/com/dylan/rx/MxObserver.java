package com.dylan.rx;

public class MxObserver<T> implements rx.Observer<T> {

    public interface OnNext<T> {
        public void onNext(T r);
    }
    public interface OnCompleted {
        public void onCompleted();
    }
    public interface OnError {
        public void onError(Throwable e);
    }

    private OnNext<T> apiNext = null;
    private OnError apiError = null;
    private OnCompleted apiCompleted = null;
    public MxObserver(OnNext<T> next) {
        apiNext = next;
    }
    public MxObserver(OnNext<T> next, OnError error) {
        apiNext = next;
        apiError = error;
    }
    public MxObserver(OnNext<T> next, OnError error, OnCompleted completed) {
        apiNext = next;
        apiError = error;
        apiCompleted = completed;
    }

    @Override
    public void onCompleted() {
        if (apiCompleted != null)apiCompleted.onCompleted();
    }
    @Override
    public void onError(Throwable e) {
        if (apiError != null)apiError.onError(e);
    }
    @Override
    public void onNext(T r) {
        if (apiNext != null)apiNext.onNext(r);
    }
}
