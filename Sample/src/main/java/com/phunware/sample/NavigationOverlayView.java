package com.phunware.sample;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.phunware.mapping.manager.Navigator;
import com.phunware.mapping.model.BuildingOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuver;
import com.phunware.mapping.model.RouteManeuverOptions;
import com.phunware.sample.util.ManeuverDisplayHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NavigationOverlayView extends ViewPager
        implements Navigator.OnManeuverChangedListener {

    private Navigator navigator;
    private ManeuverPagerAdapter adapter;

    private final OnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (navigator != null) {
                ManeuverPair pair = adapter.getItem(position);
                navigator.setCurrentManeuver(pair.mainPos);
            }
        }
    };

    public NavigationOverlayView(Context context) {
        this(context, null);
    }

    public NavigationOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNavigator(BuildingOptions building, Navigator navigator) {
        if (this.navigator != null) {
            // TODO unregister listener
        }
        this.navigator = navigator;
        navigator.addOnManeuverChangedListener(this);

        List<ManeuverPair> pairs = new ArrayList<>();
        List<RouteManeuverOptions> maneuvers = this.navigator.getManeuvers();
        for (int i = 0; i < maneuvers.size(); i += 2) {
            ManeuverPair pair = new ManeuverPair();
            pair.mainPos = i;
            pair.mainManeuver = maneuvers.get(i);

            if (i + 1 < maneuvers.size()) {
                RouteManeuverOptions next = maneuvers.get(i + 1);
                if (next.isTurnManeuver() || next.isPortalManeuver()) {
                    pair.turnPos = i + 1;
                    pair.turnManeuver = next;
                } else {
                    i--;
                }
            }

            pairs.add(pair);
        }

        setAdapter(adapter = new ManeuverPagerAdapter(navigator));
        adapter.setManeuvers(building, pairs);
        addOnPageChangeListener(pageChangeListener);
        setCurrentItem(0);
        navigator.setCurrentManeuver(0);
        // TODO add a listener to the navigator so we can automatically update when
        // current maneuver changes
    }

    @Override
    public void onManeuverChanged(Navigator navigator, int position) {
        for (int i = 0; i < adapter.getCount(); i++) {
            ManeuverPair pair = adapter.getItem(i);
            if (pair.mainPos == position || pair.turnPos == position) {
                setCurrentItem(i);
                return;
            }
        }
    }

    private static class ManeuverPair {
        int mainPos;
        int turnPos;
        RouteManeuverOptions mainManeuver;
        RouteManeuverOptions turnManeuver;
    }

    private static final class ManeuverPagerAdapter extends PagerAdapter {

        private final Navigator navigator;
        private final ManeuverDisplayHelper displayHelper;
        private final List<ManeuverPair> maneuvers = new ArrayList<>();

        public ManeuverPagerAdapter(Navigator navigator) {
            super();
            this.navigator = navigator;
            displayHelper = new ManeuverDisplayHelper();
        }

        @Override
        public int getCount() {
            return maneuvers.size();
        }

        public void setManeuvers(BuildingOptions building,
                Collection<? extends ManeuverPair> maneuvers) {
            displayHelper.setBuilding(building);
            this.maneuvers.clear();
            this.maneuvers.addAll(maneuvers);
            notifyDataSetChanged();
        }

        public ManeuverPair getItem(int position) {
            return maneuvers.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_maneuver, container, false);

            ImageView direction = (ImageView) v.findViewById(R.id.direction);
            TextView maneuver = (TextView) v.findViewById(R.id.maneuver);
            TextView nextManeuver = (TextView) v.findViewById(R.id.next_maneuver);
            ImageView nextDirection = (ImageView) v.findViewById(R.id.next_direction);

            final ManeuverPair m = maneuvers.get(position);

            direction.setImageResource(displayHelper.getImageResourceForDirection(m.mainManeuver));
            maneuver.setText(displayHelper.stringForDirection(m.mainManeuver));

            if (m.turnManeuver == null) {
                nextManeuver.setVisibility(View.GONE);
                nextDirection.setVisibility(GONE);
                v.findViewById(R.id.next).setVisibility(GONE);
            } else {
                v.findViewById(R.id.next).setVisibility(VISIBLE);
                nextManeuver.setVisibility(View.VISIBLE);
                nextDirection.setVisibility(VISIBLE);
                nextDirection.setImageResource(displayHelper
                        .getImageResourceForDirection(m.turnManeuver));
                nextManeuver.setText(displayHelper.stringForDirection(m.turnManeuver));
            }

            if (position == maneuvers.size() - 1) {
                v.findViewById(R.id.next).setVisibility(GONE);
                final int pointCount = navigator.getRoute().getPoints().size();
                PointOptions finalPoint = navigator.getRoute().getPoints().get(pointCount - 1);
                String customLocation = container.getContext().getString(R.string.custom_location_title);
                String arrive = container.getContext().getString(R.string.to_arrive,
                        finalPoint.getName() == null ? customLocation : finalPoint.getName());
                nextManeuver.setVisibility(View.VISIBLE);
                nextManeuver.setText(arrive);
            }

            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals((View) object);
        }
    }
}
