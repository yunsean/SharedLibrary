package com.dylan.common.rx;

public class RxObserver<T> implements rx.Observer<T> {

    public interface OnNext<T> {
        public void onNext(T r) throws Exception;
    }
    public interface OnCompleted {
        public void onCompleted() throws Exception;
    }
    public interface OnError {
        public void onError(Throwable e);
    }

    private OnNext<T> apiNext = null;
    private OnError apiError = null;
    private OnCompleted apiCompleted = null;
    public RxObserver() {
    }
    public RxObserver(OnNext<T> next) {
        apiNext = next;
    }
    public RxObserver(OnNext<T> next, OnError error) {
        apiNext = next;
        apiError = error;
    }
    public RxObserver(OnNext<T> next, OnError error, OnCompleted completed) {
        apiNext = next;
        apiError = error;
        apiCompleted = completed;
    }

    @Override
    public void onCompleted() {
        try {
            if (apiCompleted != null)apiCompleted.onCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void onError(Throwable e) {
        if (apiError != null)apiError.onError(e);
    }
    @Override
    public void onNext(T r) {
        try {
            if (apiNext != null)apiNext.onNext(r);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
