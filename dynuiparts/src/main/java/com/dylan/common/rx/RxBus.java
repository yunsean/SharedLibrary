package com.dylan.common.rx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {

    public RxBus() {
        bus = new SerializedSubject<>(PublishSubject.create());
    }
    public static RxBus getDefault() {
        RxBus rxBus = defaultInstance;
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                rxBus = defaultInstance;
                if (defaultInstance == null) {
                    rxBus = new RxBus();
                    defaultInstance = rxBus;
                }
            }
        }
        return rxBus;
    }
    public void post (Object o) {
        bus.onNext(o);
    }
    public <T> Observable<T> toObservable(Class<T> eventType) {
        return bus.ofType(eventType);
    }

    public void post(int code, Object o){
        bus.onNext(new Message(code, o));
    }
    public <T> Observable<T> toObservable(final int code, final Class<T> eventType) {
        return bus.ofType(Message.class)
                .filter(new Func1<Message,Boolean>() {
                    @Override
                    public Boolean call(Message o) {
                        return o.getCode() == code && eventType.isInstance(o.getObject());
                    }
                }).map(new Func1<Message,Object>() {
                    @Override
                    public Object call(Message o) {
                        return o.getObject();
                    }
                }).cast(eventType);
    }

    public <T> RxBus register(Object observer, Class<T> eventType, Action1<T> action) {
        Subscription subscription = toObservable(eventType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
        addObserver(observer, subscription);
        return this;
    }
    public <T> RxBus register(Object observer, int code, Class<T> eventType, Action1<T> action) {
        Subscription subscription = toObservable(code, eventType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
        addObserver(observer, subscription);
        return this;
    }
    public RxBus unregister(Object observer) {
        delObserver(observer);
        return this;
    }

    private final Subject bus;
    private final Map<Object, List<Subscription>> subscriptionMap = new HashMap<>();
    private static volatile RxBus defaultInstance;
    public class Message {
        private int code;
        private Object object;
        public Message(int code, Object o) {
            this.code = code;
            this.object = o;
        }
        public int getCode() {
            return code;
        }
        public Object getObject() {
            return object;
        }
    }

    private void addObserver(Object observer, Subscription subscription) {
        synchronized (subscriptionMap) {
            List<Subscription> subscriptions = subscriptionMap.get(observer);
            if (subscriptions == null) {
                subscriptions = new ArrayList<>();
                subscriptionMap.put(observer, subscriptions);
            }
            subscriptions.add(subscription);
        }
    }
    private void delObserver(Object observer) {
        synchronized (subscriptionMap) {
            List<Subscription> subscriptions = subscriptionMap.get(observer);
            if (subscriptions != null) {
                for (Subscription subscription : subscriptions) {
                    subscription.unsubscribe();
                }
                subscriptionMap.remove(observer);
            }
        }
    }
}


