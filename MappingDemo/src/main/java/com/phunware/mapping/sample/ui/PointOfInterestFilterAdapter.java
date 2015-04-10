package com.phunware.mapping.sample.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.phunware.mapping.Utils;
import com.phunware.mapping.library.util.PwPointNameComparator;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.sample.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PointOfInterestFilterAdapter extends ArrayAdapter<PwPoint> implements Filterable {

    private static final String TAG = PointOfInterestFilterAdapter.class.getSimpleName();

    private Context mContext;
    private PwBuilding mPwBuilding;
    private List<PwPoint> mOriginalItems;
    private List<PwPoint> mFilteredList;

    // Lock for getCount, performFiltering and publishResults.
    // Prevents call getCount during performFiltering method is executing.
    private Object mLock = new Object();

    public PointOfInterestFilterAdapter(Context context, PwBuilding building, List<PwPoint> pointsOfInterest) {
        this(context, building, pointsOfInterest, false);
    }

    public PointOfInterestFilterAdapter(Context context, PwBuilding building, List<PwPoint> pointsOfInterest, boolean addMyLocation) {
        super(context, R.layout.mapping_search_view_list_item, cloneList(context, pointsOfInterest, addMyLocation, false));

        if (pointsOfInterest != null) {
            Collections.sort(pointsOfInterest, new PwPointNameComparator());
        }
        this.mContext = context;
        this.mPwBuilding = building;
        this.mFilteredList = new ArrayList<PwPoint>();
        this.mOriginalItems = cloneList(context, pointsOfInterest, addMyLocation, false);
    }

    public PointOfInterestFilterAdapter(Context context, PwBuilding building, List<PwPoint> pointsOfInterest, boolean addMyLocation, boolean addFlatMarker) {
        super(context, R.layout.mapping_search_view_list_item, cloneList(context, pointsOfInterest, addMyLocation, addFlatMarker));

        if (pointsOfInterest != null) {
            Collections.sort(pointsOfInterest, new PwPointNameComparator());
        }
        this.mContext = context;
        this.mPwBuilding = building;
        this.mFilteredList = new ArrayList<PwPoint>();
        this.mOriginalItems = cloneList(context, pointsOfInterest, addMyLocation, addFlatMarker);
    }

    @Override
    public int getCount() {
        synchronized (mLock) {
            return mFilteredList.size();
        }
    }

    @Override
    public PwPoint getItem(int index) {
        if (mFilteredList != null && mFilteredList.size() > index) {
            return mFilteredList.get(index);
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                synchronized (mLock) {
                    FilterResults results = new FilterResults();

                    if (TextUtils.isEmpty(constraint)) {
                        mFilteredList.clear();
                        for (PwPoint pwPoint : mOriginalItems) {
                            mFilteredList.add(pwPoint);
                        }
                    } else if (mContext.getString(R.string.mapping_my_location).equals(constraint.toString()) && !mOriginalItems.isEmpty()) {
                        mFilteredList.clear();
                        for (PwPoint pwPoint : mOriginalItems) {
                            if (pwPoint.getId() != Long.MIN_VALUE) {
                                mFilteredList.add(pwPoint);
                            }
                        }
                    } else {
                        mFilteredList.clear();
                        for (PwPoint pwPoint : mOriginalItems) {
                            if (pwPoint.getName() == null) continue;
                            if (pwPoint.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                mFilteredList.add(pwPoint);
                            }
                        }
                    }

                    results.values = mFilteredList;
                    results.count = mFilteredList.size();

                    return results;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                synchronized (mLock) {
                    if (results != null && results.count > 0) {
                        final List<PwPoint> filterResults = (List<PwPoint>) results.values;
                        clear();
                        addAll(filterResults);
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            }
        };
        return filter;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.mapping_search_view_list_item, parent, false);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            view.setTag(new ViewHolder(text1, text2));
        }

        if (holder == null && view != null) {
            final Object tag = view.getTag();
            if (tag instanceof ViewHolder) {
                holder = (ViewHolder) tag;
            }
        }

        final PwPoint pointOfInterest = getItem(position);
        if (pointOfInterest != null && holder != null) {
            holder.text1.setText(pointOfInterest.getName());
            holder.text2.setText(Utils.findFloorNameById(pointOfInterest.getFloorId(), mPwBuilding));
        }
        return view;
    }

    public void setData(final List<PwPoint> data) {
        clear();
        if (data != null) {
            for (PwPoint pointOfInterest : data) {
                add(pointOfInterest);
            }
        }
    }

    static class ViewHolder {
        public final TextView text1;
        public final TextView text2;

        public ViewHolder(TextView text1, TextView text2) {
            this.text1 = text1;
            this.text2 = text2;
        }
    }

    public static List<PwPoint> cloneList(Context context, List<PwPoint> pwPoints, boolean addMyLocation, boolean addFlatMarker) {
        if (pwPoints == null || pwPoints.isEmpty()) {
            return null;
        }
        int size = pwPoints.size();

        if (addMyLocation) size++;
        if (addFlatMarker) size++;

        final List<PwPoint> clonedList = new ArrayList<PwPoint>(size);
        if (addMyLocation) {
            clonedList.add(new PwPoint(context.getString(R.string.mapping_my_location)));
        }
        if (addFlatMarker) {
            clonedList.add(new PwPoint(context.getString(R.string.mapping_flat_marker)));
        }
        for (PwPoint pwPoint : pwPoints) {
            if (!pwPoint.isPortal()) {
                clonedList.add(pwPoint);
            }
        }
        return clonedList;
    }
}