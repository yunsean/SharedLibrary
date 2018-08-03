package com.yunsean.dynkotlins.extensions

import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1

class Observer<T>(var observable : Observable<T>?) : rx.Observer<T> {

    override fun onError(e: Throwable) {
        this.error?.let { it(e) }
    }
    override fun onNext(t: T) {
        this.next?.let { it(t) }
    }
    override fun onCompleted() {
        this.completed?.let { it() }
    }

    private var next: ((T) -> Unit)? = null
    private var completed: (() -> Unit)? = null
    private var error: ((Throwable) -> Unit)? = null

    fun next(next : (T) -> Unit) : Observer<T> {
        this.next = next;
        return this
    }
    fun error(error : (Throwable) -> Unit) : Observer<T> {
        this.error = error
        return this
    }
    fun complete(completed : () -> Unit) : Observer<T> {
        this.completed = completed;
        return this
    }
    fun subscribe() : Subscription? {
        var result : Subscription? = null
        observable?.let { result = it.subscribe(this) }
        observable = null
        return result
    }
}

fun <T> Observable<T>.nextOnMain(next : (T) -> Unit) : Observer<T> {
    return Observer<T>(observeOn(AndroidSchedulers.mainThread()))
            .next(next)
}
fun <T> Observable<T>.errorOnMain(error : (Throwable) -> Unit) : Observer<T> {
    return Observer<T>(observeOn(AndroidSchedulers.mainThread()))
            .error(error)
}
fun <T> Observable<T>.completeOnMain(complete : () -> Unit) : Observer<T> {
    return Observer<T>(observeOn(AndroidSchedulers.mainThread()))
            .complete(complete)
}

fun <T> Observable<T>.next(next : (T) -> Unit) : Observer<T> {
    return Observer<T>(this).next(next)
}
fun <T> Observable<T>.error(error : (Throwable) -> Unit) : Observer<T> {
    return Observer<T>(this).error(error)
}
fun <T> Observable<T>.complete(completed : () -> Unit) : Observer<T> {
    return Observer<T>(this).complete(completed)
}
