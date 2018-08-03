package com.dylan.common.rx;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureError;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus2 {

    private static RxBus2 instance;
    private Subject<Object> subjectBus;
    private FlowableProcessor<Object> processorBus;

    public static RxBus2 getDefault() {
        if (instance == null) {
            synchronized (RxBus2.class) {
                if (instance == null) {
                    RxBus2 tempInstance = new RxBus2();
                    tempInstance.subjectBus = PublishSubject.create().toSerialized();
                    tempInstance.processorBus = PublishProcessor.create().toSerialized();
                    instance = tempInstance;
                }
            }
        }
        return instance;
    }

    public <T> CompositeDisposable register(Class<T> eventType, Consumer<T> observer, CompositeDisposable disposable) {
        if (disposable == null) disposable = new CompositeDisposable();
        disposable.add(toObserverable(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(observer));
        return disposable;
    }

    public <T> Disposable register(Class<T> eventType, Consumer<T> observer) {
        return toObserverable(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }
    public <T> Disposable register(Class<T> eventType, Consumer<T> observer, Scheduler scheduler) {
        return toObserverable(eventType).observeOn(scheduler).subscribe(observer);
    }
    public <T> Disposable register(Class<T> eventType, Consumer<T> observer, Scheduler scheduler, BackpressureStrategy strategy) {
        Flowable o = toFlowable(eventType);
        switch (strategy) {
            case DROP:
                o = o.onBackpressureDrop();
            case LATEST:
                o = o.onBackpressureLatest();
            case MISSING:
                o = o;
            case ERROR:
                o = RxJavaPlugins.onAssembly(new FlowableOnBackpressureError<>(o));
            default:
                o = o.onBackpressureBuffer();
        }
        if (scheduler != null) {
            o.observeOn(scheduler);
        }
        return o.subscribe(observer);
    }

    public <T> Disposable register(Class<T> eventType, Consumer<T> observer, BackpressureStrategy strategy) {
        return register(eventType, observer, null, strategy);
    }

    public void unregister(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void unregister(CompositeDisposable compositeDisposable) {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void post(final Object event) {
        subjectBus.onNext(event);
        processorBus.onNext(event);
    }

    private Observable toObserverable(Class cls) {
        return subjectBus.ofType(cls);
    }

    private Flowable toFlowable(Class cls) {
        return processorBus.ofType(cls);
    }

    public boolean hasObservers() {
        return subjectBus.hasObservers();
    }

    public boolean hasSubscribers() {
        return processorBus.hasSubscribers();
    }
}




