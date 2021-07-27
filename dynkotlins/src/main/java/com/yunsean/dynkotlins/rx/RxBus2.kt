package com.yunsean.dynkotlins.rx

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


class RxBus2 {
    private var subjectBus: Subject<Any> = PublishSubject.create<Any>().toSerialized()

    fun <T, R> register(eventType: Class<T>, composer: ObservableTransformer<in T?, out R>?, observer: Consumer<R>): RxBus2 {
        toObserverable(eventType)
            .compose(composer)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
        return this
    }

    fun <T> register(eventType: Class<T>, observer: Consumer<T>, disposable: CompositeDisposable?): CompositeDisposable {
        var disposable = disposable
        if (disposable == null) disposable = CompositeDisposable()
        disposable.add(toObserverable<T>(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(observer))
        return disposable
    }
    fun <T> register(eventType: Class<T>, observer: Consumer<T>): Disposable {
        return toObserverable<T>(eventType).observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
    }
    fun unregister(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
    fun unregister(compositeDisposable: CompositeDisposable?) {
        compositeDisposable?.clear()
    }
    fun post(event: Any) {
        subjectBus.onNext(event)
    }
    private fun <T> toObserverable(cls: Class<T>): Observable<T> = subjectBus.ofType(cls)
    fun hasObservers(): Boolean = subjectBus.hasObservers()

    private object Holder {
        val INSTANCE = RxBus2()
    }
    companion object {
        val shared: RxBus2 by lazy { Holder.INSTANCE }
    }
}
