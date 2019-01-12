package com.syv.takecare.takecare.customViews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class FeedRecyclerView extends RecyclerView {
    private View emptyFeedView; //View to be displayed when the feed is empty

    public FeedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setEmptyView(View view) {
        this.emptyFeedView = view;
        toggleVisibility();
    }

    public void toggleVisibility() {
        if (emptyFeedView != null) {
            //Make the emptyFeedView visible of the adapter has no items (feed is empty)
            emptyFeedView.setVisibility(
                    (getAdapter() == null || getAdapter().getItemCount() == 0) ? VISIBLE : GONE);
            //The list itself is set to be invisible if there are no items, in order to display emptyFeedView in its stead
            FeedRecyclerView.this.setVisibility(
                    (getAdapter() == null || getAdapter().getItemCount() == 0) ? GONE : VISIBLE);
        }
    }

    final AdapterDataObserver dataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            //Insert the item to the feed
            super.onChanged();
            //Update emptyFeedView's or the feed's visibility
            toggleVisibility();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            //Insert the item to the feed
            super.onItemRangeInserted(positionStart, itemCount);
            //Update emptyFeedView's or the feed's visibility
            toggleVisibility();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            //Insert the item to the feed
            super.onItemRangeRemoved(positionStart, itemCount);
            //Update emptyFeedView's or the feed's visibility
            toggleVisibility();
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        Adapter prevAdapter = getAdapter();
        super.setAdapter(adapter); //Set the feed's adapter
        //Assign the dataObserver to the new adapter
        if (prevAdapter != null) {
            prevAdapter.unregisterAdapterDataObserver(dataObserver);
        }
        if (adapter != null) {
            adapter.registerAdapterDataObserver(dataObserver);
        }
        toggleVisibility();
    }
}
