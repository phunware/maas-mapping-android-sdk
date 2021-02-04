package com.phunware.kotlin.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatSpinner;

public class CustomSpinner extends AppCompatSpinner {

    public CustomSpinner(Context context)
    { super(context); }

    public CustomSpinner(Context context, AttributeSet attrs)
    { super(context, attrs); }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyle)
    { super(context, attrs, defStyle); }

    @Override
    public void
    setSelection(int position, boolean animate)
    {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected && getOnItemSelectedListener() != null) {
            // Spinner does not call OnItemSelectedListener if the same item is selected, so do it manually.
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void
    setSelection(int position)
    {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected && getOnItemSelectedListener() != null) {
            // Spinner does not call OnItemSelectedListener if the same item is selected, so do it manually.
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}
