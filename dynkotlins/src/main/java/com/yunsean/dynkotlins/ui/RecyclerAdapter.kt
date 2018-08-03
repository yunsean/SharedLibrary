package com.yunsean.dynkotlins.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.layoutInflater

class RecyclerAdapter<T>(val layoutResourceId: Int, items: List<T>? = null, val init: (View, Int, T) -> Unit) :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder<T>>() {

    var items: List<T>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    init {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = parent.context.layoutInflater.inflate(layoutResourceId, parent, false)
        return ViewHolder(view, init)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.bindForecast(position, items!![position])
    }

    override fun getItemCount() = items?.size ?: 0

    class ViewHolder<in T>(view: View, val init: (View, Int, T) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(index: Int, item: T) {
            with(item) {
                try { init(itemView, index, item) } catch (ex: Exception) { ex.printStackTrace() }
            }
        }
    }
}