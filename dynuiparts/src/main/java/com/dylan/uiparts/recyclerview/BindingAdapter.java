package com.dylan.uiparts.recyclerview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BindingAdapter<T> extends RecyclerView.Adapter<BindingViewHolder> {

    private LayoutInflater mLayoutInflater = null;
    private OnItemClickListener<T> mListener = null;
    private List<T> mDataList = new ArrayList<>();
    private int mListItemResId = 0;
    private int mVariableIdResId = 0;

    public interface OnItemClickListener<T>  {
        void OnItemClick(T item);
    }

    public BindingAdapter(Context context, int listItemResId, int variableIdResId) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListItemResId = listItemResId;
        mVariableIdResId = variableIdResId;
    }

    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        binding = DataBindingUtil.inflate(mLayoutInflater, mListItemResId, parent, false);
        return new BindingViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        final T item = mDataList.get(position);
        holder.getBinding().setVariable(mVariableIdResId, item);
        holder.getBinding().executePendingBindings();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnItemClick(item);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return mDataList.size();
    }
    public List<T> getDataList() {
        return mDataList;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void addAll(List<T> items) {
        mDataList.addAll(items);
        notifyDataSetChanged();
    }
    public void add(T item) {
        mDataList.add(item);
        notifyItemInserted(mDataList.size() - 1);
    }
    public void remove(T item) {
        mDataList.remove(item);
        notifyDataSetChanged();
    }
    public void clear() {
        mDataList.clear();
        notifyDataSetChanged();
    }
}
