package com.yunsean.dynkotlins.extensions

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

interface Observer2Subscriber {
    fun addDisposable(disposable: Disposable)
    fun removeDisposable(disposable: Disposable)
}

class Observer2<T>(private var observable: Observable<T>? = null) : io.reactivex.Observer<T>, Disposable {

    override fun onSubscribe(d: Disposable) {
        this.disposable = d;
        this.subscribe?.let { it(d) }
        this.subscriber2?.addDisposable(d)
    }
    override fun onError(e: Throwable) {
        this.error?.let { it(e) }
        this.disposable?.let { this.subscriber2?.removeDisposable(it) }
    }
    override fun onNext(t: T) {
        this.next?.let { it(t) }
    }
    override fun onComplete() {
        this.complete?.let { it() }
        this.disposable?.let { this.subscriber2?.removeDisposable(it) }
    }

    private var disposable : Disposable? = null;
    private var subscribe: ((Disposable) -> Unit)? = null
    private var next: ((T) -> Unit)? = null
    private var complete: (() -> Unit)? = null
    private var error: ((Throwable) -> Unit)? = null
    private var subscriber2: Observer2Subscriber? = null

    fun next(next : (T) -> Unit) : Observer2<T> {
        this.next = next
        return this
    }
    fun error(error : (Throwable) -> Unit) : Observer2<T> {
        this.error = error
        return this
    }
    fun complete(complete : () -> Unit) : Observer2<T> {
        this.complete = complete
        return this
    }
    fun subscribed(subscribe: ((Disposable) -> Unit)? = null) : Observer2<T> {
        this.subscribe = subscribe
        return this
    }
    fun subscribed(subscriber: Observer2Subscriber? = null) : Observer2<T> {
        this.subscriber2 = subscriber
        return this
    }
    fun subscribe(subscribe: ((Disposable) -> Unit)? = null) {
        this.subscribe = subscribe
        this.observable?.subscribe(this)
        this.observable = null
    }
    fun subscribe(subscriber: Observer2Subscriber? = null) {
        this.subscriber2 = subscriber
        this.observable?.subscribe(this)
        this.observable = null
    }
    fun subscribeOnMain(subscribe: ((Disposable) -> Unit)? = null) {
        this.subscribe = subscribe
        this.observable
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(this)
        this.observable = null
    }
    fun subscribeOnMain(subscriber: Observer2Subscriber? = null) {
        this.subscriber2 = subscriber
        this.observable
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(this)
        this.observable = null
    }

    override fun dispose() = this.disposable?.dispose() ?: Unit
    override fun isDisposed(): Boolean = disposable?.isDisposed ?: false
}

fun <T> Observable<T>.nextOnMain(next : (T) -> Unit) : Observer2<T> {
    return observeOn(AndroidSchedulers.mainThread())
            .subscribeObserver(Observer2<T>())
            .next(next)
}
fun <T> Observable<T>.errorOnMain(error : (Throwable) -> Unit) : Observer2<T> {
    return observeOn(AndroidSchedulers.mainThread())
            .subscribeObserver(Observer2<T>())
            .error(error)
}
fun <T> Observable<T>.completeOnMain(complete : () -> Unit) : Observer2<T> {
    return observeOn(AndroidSchedulers.mainThread())
            .subscribeObserver(Observer2<T>())
            .complete(complete)
}

fun <T> Observable<T>.withNext(next : (T) -> Unit) : Observer2<T> {
    return Observer2<T>(this).next(next)
}
fun <T> Observable<T>.withError(error : (Throwable) -> Unit) : Observer2<T> {
    return Observer2<T>(this).error(error)
}
fun <T> Observable<T>.withComplete(completed : () -> Unit) : Observer2<T> {
    return Observer2<T>(this).complete(completed)
}

fun <T> Observable<T>.next(next : (T) -> Unit) : Observer2<T> {
    return subscribeObserver(Observer2<T>().next(next))
}
fun <T> Observable<T>.error(error : (Throwable) -> Unit) : Observer2<T> {
    return subscribeObserver(Observer2<T>().error(error))
}
fun <T> Observable<T>.complete(completed : () -> Unit) : Observer2<T> {
    return subscribeObserver(Observer2<T>().complete(completed))
}
fun <T> Observable<T>.subscribed(completed : (Disposable) -> Unit) : Observer2<T> {
    return subscribeObserver(Observer2<T>().subscribed(completed))
}
fun <T> Observable<T>.subscribed(subscriber: Observer2Subscriber) : Observer2<T> {
    return subscribeObserver(Observer2<T>().subscribed(subscriber))
}

fun <T> Observable<T>.subscribeObserver(observer: Observer2<T>) : Observer2<T> {
    this.subscribe(observer)
    return observer
}