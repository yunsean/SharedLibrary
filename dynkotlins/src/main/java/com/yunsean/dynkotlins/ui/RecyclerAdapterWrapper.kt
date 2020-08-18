package com.yunsean.dynkotlins.ui

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.View
import android.view.ViewGroup

class RecyclerAdapterWrapper<T: androidx.recyclerview.widget.RecyclerView.ViewHolder>(
        val adapter: androidx.recyclerview.widget.RecyclerView.Adapter<T>) : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerAdapterWrapper.Companion.ViewHolder<T>>() {

    private val headerViews = androidx.collection.SparseArrayCompat<View>()
    private val footViews = androidx.collection.SparseArrayCompat<View>()

    private val realItemCount: Int
        get() = adapter.itemCount
    val headersCount: Int
        get() = if (showHeaders) headerViews.size() else 0
    val footersCount: Int
        get() = if (showFooters) footViews.size() else 0
    var showHeaders: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var showFooters: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addHeaderView(view: View): RecyclerAdapterWrapper<T> {
        headerViews.put(headerViews.size() + BASE_ITEM_TYPE_HEADER, view)
        return this
    }
    fun addFootView(view: View): RecyclerAdapterWrapper<T> {
        footViews.put(footViews.size() + BASE_ITEM_TYPE_FOOTER, view)
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return if (headerViews.get(viewType) != null) ViewHolder<T>(true, headerViews.get(viewType)!!)
        else if (footViews.get(viewType) != null) ViewHolder<T>(true, footViews.get(viewType)!!)
        else ViewHolder(adapter.onCreateViewHolder(parent, viewType))
    }
    override fun getItemViewType(position: Int): Int {
        return if (isHeaderViewPos(position)) headerViews.keyAt(position)
        else if (isFooterViewPos(position)) footViews.keyAt(position - headersCount - realItemCount)
        else adapter.getItemViewType(position - headersCount)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder?.holder?.let { adapter.onBindViewHolder(it, position - headersCount) }
    }
    override fun getItemCount(): Int = headersCount + footersCount + realItemCount
    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        onAttachedToRecyclerView(adapter, recyclerView, object : SpanSizeCallback {
            override fun getSpanSize(layoutManager: androidx.recyclerview.widget.GridLayoutManager, oldLookup: androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup, position: Int): Int {
                val viewType = getItemViewType(position)
                return if (headerViews.get(viewType) != null) layoutManager.getSpanCount()
                else if (footViews.get(viewType) != null) layoutManager.getSpanCount()
                else oldLookup.getSpanSize(position)
            }
        })
    }

    override fun onViewAttachedToWindow(holder: ViewHolder<T>) {
        holder?.let {
            it.holder?.let { adapter.onViewAttachedToWindow(it) } ?: setFullSpan(it)
        }
    }

    private interface SpanSizeCallback {
        fun getSpanSize(layoutManager: androidx.recyclerview.widget.GridLayoutManager, oldLookup: androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup, position: Int): Int
    }
    private fun onAttachedToRecyclerView(innerAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>, recyclerView: androidx.recyclerview.widget.RecyclerView, callback: SpanSizeCallback) {
        innerAdapter.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
            val spanSizeLookup = layoutManager.spanSizeLookup
            layoutManager.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return callback.getSpanSize(layoutManager, spanSizeLookup, position)
                }
            }
            layoutManager.spanCount = layoutManager.spanCount
        }
    }
    private fun setFullSpan(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams) {
            lp.setFullSpan(true)
        }
    }

    private fun isHeaderViewPos(position: Int): Boolean = position < headersCount
    private fun isFooterViewPos(position: Int): Boolean = position >= headersCount + realItemCount
    companion object {
        private val BASE_ITEM_TYPE_HEADER = 100000
        private val BASE_ITEM_TYPE_FOOTER = 200000
        class ViewHolder<T: androidx.recyclerview.widget.RecyclerView.ViewHolder>(val headerOrFooter: Boolean, view: View):
                androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            constructor(holder: T): this(false, holder.itemView) { this.holder = holder }
            var holder: T? = null
        }
    }
}