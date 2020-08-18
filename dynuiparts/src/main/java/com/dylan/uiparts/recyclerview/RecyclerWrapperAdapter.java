package com.dylan.uiparts.recyclerview;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RecyclerWrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    protected static final int HEADER = Integer.MIN_VALUE;
    protected static final int FOOTER = Integer.MAX_VALUE;
    private RecyclerView.Adapter mAdapter = null;
    private LinearLayout mHeaderContainer = null;
    private LinearLayout mFooterContainer = null;

    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            RecyclerWrapperAdapter.this.notifyDataSetChanged();
        }
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            RecyclerWrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount);
        }
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            RecyclerWrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount, payload);
        }
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            RecyclerWrapperAdapter.this.notifyItemRangeInserted(positionStart + 2, itemCount);
        }
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            RecyclerWrapperAdapter.this.notifyItemRangeRemoved(positionStart + 2, itemCount);
        }
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            RecyclerWrapperAdapter.this.notifyDataSetChanged();
        }
    };

    public RecyclerWrapperAdapter(Context context, RecyclerView.Adapter adapter, LinearLayout headerContainer, LinearLayout footerContainer) {
        this.mAdapter = adapter;
        this.mHeaderContainer = headerContainer;
        this.mFooterContainer = footerContainer;
        ensureHeaderViewContainer(context);
        ensureFooterViewContainer(context);
        mAdapter.registerAdapterDataObserver(mObserver);
    }
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    RecyclerWrapperAdapter wrapperAdapter = (RecyclerWrapperAdapter) recyclerView.getAdapter();
                    if (isFullSpanType(wrapperAdapter.getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position - 2);
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }

    private boolean isFullSpanType(int type) {
        return type == HEADER || type == FOOTER;
    }
    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + 2;
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        } else if (0 < position && position < mAdapter.getItemCount() + 1) {
            return mAdapter.getItemViewType(position - 1);
        } else if (position == mAdapter.getItemCount() + 1) {
            return FOOTER;
        }
        throw new IllegalArgumentException("Wrong type! Position = " + position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            return new HeaderContainerViewHolder(mHeaderContainer);
        } else if (viewType == FOOTER) {
            return new FooterContainerViewHolder(mFooterContainer);
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (0 < position && position < mAdapter.getItemCount() + 1) {
            mAdapter.onBindViewHolder(holder, position - 1);
        }
    }

    static class HeaderContainerViewHolder extends RecyclerView.ViewHolder {
        public HeaderContainerViewHolder(View itemView) {
            super(itemView);
        }
    }
    static class FooterContainerViewHolder extends RecyclerView.ViewHolder {
        public FooterContainerViewHolder(View itemView) {
            super(itemView);
        }
    }

    private void ensureHeaderViewContainer(Context context) {
        if (mHeaderContainer == null) {
            mHeaderContainer = new LinearLayout(context);
            mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
            mHeaderContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        } else {
            mHeaderContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
    private void ensureFooterViewContainer(Context context) {
        if (mFooterContainer == null) {
            mFooterContainer = new LinearLayout(context);
            mFooterContainer.setOrientation(LinearLayout.VERTICAL);
            mFooterContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        } else {
            mFooterContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
}
