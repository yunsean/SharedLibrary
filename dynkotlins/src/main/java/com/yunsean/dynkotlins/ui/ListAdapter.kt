package com.yunsean.dynkotlins.ui

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.jetbrains.anko.layoutInflater

class ListAdapter<T>(val layoutResourceId: Int, items: List<T>? = null, val init: (View, T) -> Unit)
    : BaseAdapter() {

    var items: List<T>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    init {
        this.items = items
    }

    override fun getCount() = items?.size ?: 0
    override fun getItem(i: Int): Any = items!!.get(i) as Any
    override fun getItemId(i: Int): Long = 0
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = viewGroup.context.layoutInflater.inflate(layoutResourceId, viewGroup, false)
        }
        init(view!!, items!![i])
        return view!!
    }
}