package com.yoga.utils

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.yunsean.dynkotlins.ui.SimpleItemTouchHelperCallback
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk25.coroutines.onClick

class AddibleRecylerAdapter<T>(val layoutResourceId: Int,
                               var items: MutableList<T>? = null,
                               val init: (View, Int, T, ViewHolder<T>) -> Unit) :
        RecyclerView.Adapter<AddibleRecylerAdapter.ViewHolder<T>>(),
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private var addLayoutResId: Int = 0
    private var onAddClicked: (()->Unit)? = null

    fun setupAddible(addLayoutResId: Int, onAdd: ()->Unit): AddibleRecylerAdapter<T> {
        this.addLayoutResId = addLayoutResId
        this.onAddClicked = onAdd
        return this
    }
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        items?.let {
            val panel = it.get(fromPosition)
            it.removeAt(fromPosition)
            it.add(toPosition, panel)
            notifyItemMoved(fromPosition, toPosition)
        }
        return false
    }
    override fun onItemDismiss(position: Int) {
        items?.removeAt(position)
        notifyItemRemoved(position)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = parent.context.layoutInflater.inflate(viewType, parent, false)
        return ViewHolder(view, init)
    }
    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        items?.let {
            if (position < it.size) holder.bindForecast(position, it[position])
            else holder.itemView.onClick { onAddClicked?.invoke() }
        } ?: holder.itemView.onClick { onAddClicked?.invoke() }
    }
    override fun getItemViewType(position: Int): Int {
        if (position < items?.size ?: 0) return layoutResourceId
        else return addLayoutResId
    }
    override fun getItemCount() = (items?.size ?: 0) + (if (addLayoutResId == 0) 0 else 1)

    class ViewHolder<T>(view: View, val init: (View, Int, T, ViewHolder<T>) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(index: Int, item: T) {
            try { init(itemView, index, item, this@ViewHolder) } catch (ex: Exception) { ex.printStackTrace() }
        }
    }
}