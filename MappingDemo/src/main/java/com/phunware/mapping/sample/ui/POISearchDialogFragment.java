package com.phunware.mapping.sample.ui;

import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.Button;

import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.sample.R;

import java.util.ArrayList;

/**
 * Created by vkodali on 9/17/15.
 */
public class POISearchDialogFragment extends DialogFragment {

    public static final String TAG = POISearchDialogFragment.class.getSimpleName();

    private static final String KEY_BUILDING = "KEY_BUILDING";
    private static final String KEY_POINTS_OF_INTEREST = "KEY_POINTS_OF_INTEREST";
    private static final String KEY_START_POINT_OF_INTEREST = "KEY_START_POINT_OF_INTEREST";
    //Data
    private PwBuilding mPwBuilding;
    private ArrayList<PwPoint> mPointsOfInterest;
    private PwPoint mStartPointOfInterest;
     //Views
    private InstantAutoCompleteTextView mStartAutoCompleteTextView;
    private Button mDoneButton;
    //Adapters
    private PointOfInterestFilterAdapter mStartPointOfInterestFilterAdapter;
    private PwPOISearchListener mPOISearch;

    public interface PwPOISearchListener {
        public void onPOISearchRequested(final PwPoint startPoint);
    }

   static POISearchDialogFragment newInstance(PwBuilding pwBuilding, ArrayList<PwPoint> pointsOfInterest) {
       POISearchDialogFragment fragment = new POISearchDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_BUILDING, pwBuilding);
        args.putParcelableArrayList(KEY_POINTS_OF_INTEREST, pointsOfInterest);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.mapping_fragment_poi_search, container, false);

        mStartAutoCompleteTextView = (InstantAutoCompleteTextView) rootView.findViewById(R.id.start_navigation_autocompletetextview);
       mDoneButton = (Button) rootView.findViewById(R.id.button_done);
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mPwBuilding = args.getParcelable(KEY_BUILDING);
            mPointsOfInterest = args.getParcelableArrayList(KEY_POINTS_OF_INTEREST);

        } else {
            mPwBuilding = savedInstanceState.getParcelable(KEY_BUILDING);
            mPointsOfInterest = savedInstanceState.getParcelableArrayList(KEY_POINTS_OF_INTEREST);

            mStartPointOfInterest = savedInstanceState.getParcelable(KEY_START_POINT_OF_INTEREST);

        }
      mDoneButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (mPOISearch != null) {
                  mPOISearch.onPOISearchRequested(mStartPointOfInterest);
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
         }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPwBuilding = null;
        mPointsOfInterest = null;
        mStartAutoCompleteTextView = null;
        mStartPointOfInterest = null;
        mStartPointOfInterestFilterAdapter = null;
        mDoneButton = null;
    }

    //TODO: Look at this. Clean up. <3 Robert
    private void setupViews() {
        if (mStartPointOfInterestFilterAdapter == null) {
            mStartPointOfInterestFilterAdapter = new PointOfInterestFilterAdapter(getActivity(), mPwBuilding, mPointsOfInterest, false, false);
            mStartAutoCompleteTextView.setAdapter(mStartPointOfInterestFilterAdapter);

            mStartAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PwPoint startPoint = mStartPointOfInterestFilterAdapter.getItem(position);
                    mStartPointOfInterest = startPoint;
                    enableDoneButton();
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

    }

    public void  setPOISearchListener(PwPOISearchListener POISearchListener) {
        this.mPOISearch = POISearchListener;
    }

    private void enableDoneButton() {
        final boolean enableDoneButton = (!TextUtils.isEmpty(mStartAutoCompleteTextView.getText()) && mStartPointOfInterest != null);
        mDoneButton.setEnabled(enableDoneButton);
    }

}
