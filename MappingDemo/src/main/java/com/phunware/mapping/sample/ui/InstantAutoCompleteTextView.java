package com.phunware.mapping.sample.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

import com.phunware.mapping.model.PwPoint;

public class InstantAutoCompleteTextView extends AutoCompleteTextView {

    public InstantAutoCompleteTextView(Context context) {
        super(context);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            post(new Runnable() {
                @Override
                public void run() {
                    performFiltering("", 0);
                }
            });
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_UP == event.getAction()) {
            showDropDown();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        if (selectedItem instanceof PwPoint) {
            PwPoint point = (PwPoint) selectedItem;
            return point.getName();
        }
        return super.convertSelectionToString(selectedItem);
    }

    public void swapText(CharSequence text) {
        super.replaceText(text);
    }
}