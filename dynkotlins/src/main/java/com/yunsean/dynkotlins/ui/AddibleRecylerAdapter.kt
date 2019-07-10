package com.yunsean.dynkotlins.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import cn.com.thinkwatch.ihass2.R
import cn.com.thinkwatch.ihass2.ui.AutomationEditActivity
import com.yunsean.dynkotlins.ui.RecyclerAdapter
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk25.coroutines.onClick

class AddibleRecylerAdapter<T>(val layoutResourceId: Int,
                               val items: MutableList<T>,
                               val init: (View, Int, T, ViewHolder<T>) -> Unit) :
        RecyclerView.Adapter<AddibleRecylerAdapter.ViewHolder<T>>(),
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private var createLayoutResId: Int = 0
    private var onCreateClicked: (()->Unit)? = null

    fun setOnCreateClicked(createLayoutResId: Int, onCreate: ()->Unit): AddibleRecylerAdapter<T> {
        this.createLayoutResId = createLayoutResId
        this.onCreateClicked = onCreate
        return this
    }
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        items.let {
            val panel = it.get(fromPosition)
            it.removeAt(fromPosition)
            it.add(toPosition, panel)
            notifyItemMoved(fromPosition, toPosition)
        }
        return false
    }
    override fun onItemDismiss(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = parent.context.layoutInflater.inflate(viewType, parent, false)
        return ViewHolder(view, init)
    }
    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        if (position < items.size) holder.bindForecast(position, items[position])
        else holder.itemView.onClick { onCreateClicked?.invoke() }
    }
    override fun getItemViewType(position: Int): Int {
        if (position < items.size) return layoutResourceId
        else return createLayoutResId
    }
    override fun getItemCount() = items.size + (if (createLayoutResId == 0) 0 else 1)

    class ViewHolder<T>(view: View, val init: (View, Int, T, ViewHolder<T>) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(index: Int, item: T) {
            with(item) {
                try { init(itemView, index, item, this@ViewHolder) } catch (ex: Exception) { ex.printStackTrace() }
            }
        }
    }
}