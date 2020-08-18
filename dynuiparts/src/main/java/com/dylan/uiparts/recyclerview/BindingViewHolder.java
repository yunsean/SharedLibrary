package com.dylan.uiparts.recyclerview;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class BindingViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
    private T mBinding = null;
    public BindingViewHolder(T binding) {
        super(binding.getRoot());
        mBinding = binding;
    }
    public T getBinding() {
        return mBinding;
    }
}