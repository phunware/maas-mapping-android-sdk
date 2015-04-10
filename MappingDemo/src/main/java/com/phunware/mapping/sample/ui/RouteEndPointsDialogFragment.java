package com.phunware.mapping.sample.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.phunware.core.PwLog;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.sample.R;

import java.util.ArrayList;

public class RouteEndPointsDialogFragment extends DialogFragment {
    public static final String TAG = RouteEndPointsDialogFragment.class.getSimpleName();

    private static final String KEY_BUILDING = "KEY_BUILDING";
    private static final String KEY_POINTS_OF_INTEREST = "KEY_POINTS_OF_INTEREST";
    private static final String KEY_START_POINT_OF_INTEREST = "KEY_START_POINT_OF_INTEREST";
    private static final String KEY_DESTINATION_POINT_OF_INTEREST = "KEY_DESTINATION_POINT_OF_INTEREST";
    private static final String KEY_HAS_LOCATION = "KEY_HAS_LOCATION";
    private static final String KEY_HAS_FLAT_MARKER = "KEY_HAS_FLAT_MARKER";

    //Data
    private PwBuilding mPwBuilding;
    private ArrayList<PwPoint> mPointsOfInterest;
    private PwPoint mDestinationPointOfInterest;
    private PwPoint mStartPointOfInterest;
    private boolean mHasLocation;
    private boolean mHasFlatMarker;

    //Views
    private InstantAutoCompleteTextView mStartAutoCompleteTextView;
    private InstantAutoCompleteTextView mEndAutoCompleteTextView;
    private Button mDoneButton;
    private CheckBox mIsAccessibleRouteCheckBox;

    //Adapters
    private PointOfInterestFilterAdapter mStartPointOfInterestFilterAdapter;
    private PointOfInterestFilterAdapter mEndPointOfInterestFilterAdapter;

    private PwRouteRequestedListener mPwRouteRequested;

    public interface PwRouteRequestedListener {
        public void onRouteRequested(final PwPoint startPoint, final PwPoint endPoint, final boolean isAccessible);
    }

    static RouteEndPointsDialogFragment newInstance(PwBuilding pwBuilding, ArrayList<PwPoint> pointsOfInterest, PwPoint destinationPOI) {
        return newInstance(pwBuilding, pointsOfInterest, destinationPOI, false);
    }

    static RouteEndPointsDialogFragment newInstance(PwBuilding pwBuilding, ArrayList<PwPoint> pointsOfInterest, PwPoint destinationPOI, boolean hasLocation) {
        return newInstance(pwBuilding, pointsOfInterest, destinationPOI, hasLocation, false);
    }

    static RouteEndPointsDialogFragment newInstance(PwBuilding pwBuilding, ArrayList<PwPoint> pointsOfInterest, PwPoint destinationPOI, boolean hasLocation, boolean hasFlatMarker) {
        RouteEndPointsDialogFragment fragment = new RouteEndPointsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_BUILDING, pwBuilding);
        args.putParcelableArrayList(KEY_POINTS_OF_INTEREST, pointsOfInterest);
        args.putParcelable(KEY_DESTINATION_POINT_OF_INTEREST, destinationPOI);
        args.putBoolean(KEY_HAS_LOCATION, hasLocation);
        args.putBoolean(KEY_HAS_FLAT_MARKER, hasFlatMarker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.mapping_fragment_route_directions_entry, container, false);

        mStartAutoCompleteTextView = (InstantAutoCompleteTextView) rootView.findViewById(R.id.start_navigation_autocompletetextview);
        mEndAutoCompleteTextView = (InstantAutoCompleteTextView) rootView.findViewById(R.id.end_navigation_autocompletetextview);
        final ImageButton swapRouteImageButton = (ImageButton) rootView.findViewById(R.id.swap_route_imageButton);
        mDoneButton = (Button) rootView.findViewById(R.id.button_done);
        mIsAccessibleRouteCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox_is_accessible);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mPwBuilding = args.getParcelable(KEY_BUILDING);
            mPointsOfInterest = args.getParcelableArrayList(KEY_POINTS_OF_INTEREST);
            mDestinationPointOfInterest = args.getParcelable(KEY_DESTINATION_POINT_OF_INTEREST);
            mHasLocation = args.getBoolean(KEY_HAS_LOCATION, false);
            mHasFlatMarker = args.getBoolean(KEY_HAS_FLAT_MARKER, false);
        } else {
            mPwBuilding = savedInstanceState.getParcelable(KEY_BUILDING);
            mPointsOfInterest = savedInstanceState.getParcelableArrayList(KEY_POINTS_OF_INTEREST);
            mDestinationPointOfInterest = savedInstanceState.getParcelable(KEY_DESTINATION_POINT_OF_INTEREST);
            mStartPointOfInterest = savedInstanceState.getParcelable(KEY_START_POINT_OF_INTEREST);
            mHasLocation = savedInstanceState.getBoolean(KEY_HAS_LOCATION, false);
        }

        swapRouteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence startPointOfInterestText = mStartAutoCompleteTextView.getText();
                final CharSequence endPointOfInterestText = mEndAutoCompleteTextView.getText();
                final PwPoint startPointOfInterest = mStartPointOfInterest;
                final PwPoint endPointOfInterest = mDestinationPointOfInterest;
                mStartAutoCompleteTextView.swapText(endPointOfInterestText);
                mEndAutoCompleteTextView.swapText(startPointOfInterestText);
                mStartPointOfInterest = endPointOfInterest;
                mDestinationPointOfInterest = startPointOfInterest;
                enableDoneButton();
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPwRouteRequested != null) {
                    mPwRouteRequested.onRouteRequested(mStartPointOfInterest, mDestinationPointOfInterest, mIsAccessibleRouteCheckBox.isChecked());
                }
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupViews();
        enableDoneButton();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_BUILDING, mPwBuilding);
        outState.putParcelableArrayList(KEY_POINTS_OF_INTEREST, mPointsOfInterest);
        outState.putParcelable(KEY_START_POINT_OF_INTEREST, mStartPointOfInterest);
        outState.putParcelable(KEY_DESTINATION_POINT_OF_INTEREST, mDestinationPointOfInterest);
        outState.putBoolean(KEY_HAS_LOCATION, mHasLocation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPwBuilding = null;
        mDestinationPointOfInterest = null;
        mEndAutoCompleteTextView = null;
        mPointsOfInterest = null;
        mStartAutoCompleteTextView = null;
        mStartPointOfInterest = null;
        mStartPointOfInterestFilterAdapter = null;
        mEndPointOfInterestFilterAdapter = null;
        mDoneButton = null;
    }

    //TODO: Look at this. Clean up. <3 Robert
    private void setupViews() {
        if (mStartPointOfInterestFilterAdapter == null) {

            if (mHasLocation) {
                mStartPointOfInterest = new PwPoint(getString(R.string.mapping_my_location));
                mStartAutoCompleteTextView.setText(mStartPointOfInterest.getName());
            }

            if (mHasFlatMarker) {
                mStartPointOfInterest = new PwPoint(getString(R.string.mapping_flat_marker));
                mStartAutoCompleteTextView.setText(mStartPointOfInterest.getName());
            }

            mStartPointOfInterestFilterAdapter = new PointOfInterestFilterAdapter(getActivity(), mPwBuilding, mPointsOfInterest, mHasLocation, mHasFlatMarker);
            mStartAutoCompleteTextView.setAdapter(mStartPointOfInterestFilterAdapter);

            mStartAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PwPoint startPoint = mStartPointOfInterestFilterAdapter.getItem(position);
                    mStartPointOfInterest = startPoint;
                    if (!TextUtils.isEmpty(mEndAutoCompleteTextView.getText()) && mDestinationPointOfInterest != null) {
                        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(mStartAutoCompleteTextView.getWindowToken(), 0);
                        enableDoneButton();
                    }
                }
            });
            mStartAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (mStartPointOfInterestFilterAdapter != null && mStartPointOfInterestFilterAdapter.getCount() > 0
                        && !mStartPointOfInterestFilterAdapter.getItem(0).getName().equalsIgnoreCase(mStartAutoCompleteTextView.getText().toString()))
                        mStartPointOfInterest = null;
                    else if (mStartPointOfInterestFilterAdapter != null && mStartPointOfInterestFilterAdapter.getCount() > 0)
                        mStartPointOfInterest = mStartPointOfInterestFilterAdapter.getItem(0);
                    enableDoneButton();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        if (mEndPointOfInterestFilterAdapter == null) {
            mEndPointOfInterestFilterAdapter = new PointOfInterestFilterAdapter(getActivity(), mPwBuilding, mPointsOfInterest, mHasLocation, mHasFlatMarker);
            mEndAutoCompleteTextView.setAdapter(mEndPointOfInterestFilterAdapter);
            mEndAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mDestinationPointOfInterest = mEndPointOfInterestFilterAdapter.getItem(position);
                    if (!TextUtils.isEmpty(mStartAutoCompleteTextView.getText()) && mStartPointOfInterest != null) {
                        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(mEndAutoCompleteTextView.getWindowToken(), 0);
                        enableDoneButton();
                    }
                }
            });
            final PwPoint destinationPointOfInterest = getArguments().getParcelable(KEY_DESTINATION_POINT_OF_INTEREST);
            if (destinationPointOfInterest != null) {
                mEndAutoCompleteTextView.setText(destinationPointOfInterest.getName());
                mDestinationPointOfInterest = destinationPointOfInterest;
            }
            mEndAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    if (mEndPointOfInterestFilterAdapter != null && mEndPointOfInterestFilterAdapter.getCount() > 0
                        && !mEndPointOfInterestFilterAdapter.getItem(0).getName().equalsIgnoreCase(mEndAutoCompleteTextView.getText().toString
                        ()))
                        mDestinationPointOfInterest = null;
                    else if (mEndPointOfInterestFilterAdapter != null && mEndPointOfInterestFilterAdapter.getCount() > 0)
                        mDestinationPointOfInterest = mEndPointOfInterestFilterAdapter.getItem(0);
                    enableDoneButton();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    public void setPwRouteRequestedListener(PwRouteRequestedListener pwRouteRequestedListener) {
        this.mPwRouteRequested = pwRouteRequestedListener;
    }

    private void enableDoneButton() {
        final boolean enableDoneButton = (!TextUtils.isEmpty(mStartAutoCompleteTextView.getText()) && mStartPointOfInterest != null)
            && (!TextUtils.isEmpty(mEndAutoCompleteTextView.getText()) && mDestinationPointOfInterest != null);
        mDoneButton.setEnabled(enableDoneButton);
    }
}