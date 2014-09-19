package com.phunware.mapping.sample.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MenuItemSelectionDialogFragment extends DialogFragment {
    private static final String ARG_SELECTED_ITEM = "arg_selected_item";
    private static final String ARG_ARRAY_RESOURCE_ID = "arg_array_resource_id";
    private static final String ARG_TITLE_RESOURCE_ID = "arg_title_resource_id";
    private static final String ARG_ARRAY = "arg_array";

    private DialogInterface.OnClickListener mOnClickListener;

    public static MenuItemSelectionDialogFragment newInstance(final int selectedItemIndex, final int arrayResourceId, final int dialogTitleResourceId) {
        return newInstance(selectedItemIndex, arrayResourceId, null, dialogTitleResourceId);
    }

    public static MenuItemSelectionDialogFragment newInstance(final int selectedItemIndex, final CharSequence[] itemArray, final int dialogTitleResourceId) {
        return newInstance(selectedItemIndex, 0, itemArray, dialogTitleResourceId);
    }

    private static MenuItemSelectionDialogFragment newInstance(final int selectedItemIndex, final int arrayResourceId,
                                                               final CharSequence[] itemArray, final int dialogTitleResourceId) {
        final Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_ITEM, selectedItemIndex);
        args.putInt(ARG_ARRAY_RESOURCE_ID, arrayResourceId);
        args.putInt(ARG_TITLE_RESOURCE_ID, dialogTitleResourceId);
        args.putCharSequenceArray(ARG_ARRAY, itemArray);

        MenuItemSelectionDialogFragment instance = new MenuItemSelectionDialogFragment();
        instance.setArguments(args);

        return instance;
    }

    public MenuItemSelectionDialogFragment() {
    }

    public void setOnClickListener(DialogInterface.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle arguments = getArguments();

        builder.setTitle(arguments.getInt(ARG_TITLE_RESOURCE_ID));
        if (arguments.getInt(ARG_ARRAY_RESOURCE_ID, 0) != 0) {
            builder.setSingleChoiceItems(arguments.getInt(ARG_ARRAY_RESOURCE_ID), arguments.getInt(ARG_SELECTED_ITEM, 0), mOnClickListener);
        } else {
            builder.setSingleChoiceItems(arguments.getCharSequenceArray(ARG_ARRAY), arguments.getInt(ARG_SELECTED_ITEM, 0), mOnClickListener);
        }

        return builder.create();
    }
}