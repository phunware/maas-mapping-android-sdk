package com.phunware.mapping.sample.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class StopLoadingBuildingDialogFragment extends DialogFragment {

    private DialogInterface.OnClickListener mOnClickListener;

    public void setOnClickListener(DialogInterface.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Stop Loading Building");
        builder.setNegativeButton("Cancel", mOnClickListener);

        return builder.create();
    }

}
