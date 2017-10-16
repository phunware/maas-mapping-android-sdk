package com.phunware.java.sample;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.phunware.core.PwLog;
import com.phunware.mapping.manager.Navigator;
import com.phunware.mapping.model.BuildingOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuverOptions;

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

    public void setNavigator(Navigator navigator) {
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

        adapter = new ManeuverPagerAdapter(navigator);
        setAdapter(adapter);
        adapter.setManeuvers(pairs);
        addOnPageChangeListener(pageChangeListener);
        setCurrentItem(0);
        navigator.setCurrentManeuver(0);
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

    @Override
    public void onRouteSnapFailed() {
        PwLog.e("NavigationOverlayView", "Off route");
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

        ManeuverPagerAdapter(Navigator navigator) {
            super();
            this.navigator = navigator;
            displayHelper = new ManeuverDisplayHelper();
        }

        @Override
        public int getCount() {
            return maneuvers.size();
        }

        void setManeuvers(Collection<? extends ManeuverPair> maneuvers) {
            this.maneuvers.clear();
            this.maneuvers.addAll(maneuvers);
            notifyDataSetChanged();
        }

        ManeuverPair getItem(int position) {
            return maneuvers.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = container.getContext();
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_maneuver, container, false);

            ImageView direction = v.findViewById(R.id.direction);
            TextView maneuver = v.findViewById(R.id.maneuver);
            TextView nextManeuver = v.findViewById(R.id.next_maneuver);
            ImageView nextDirection = v.findViewById(R.id.next_direction);

            final ManeuverPair m = maneuvers.get(position);

            direction.setImageResource(displayHelper.getImageResourceForDirection(context,
                    m.mainManeuver));
            maneuver.setText(displayHelper.stringForDirection(context, m.mainManeuver));

            if (m.turnManeuver == null) {
                nextManeuver.setVisibility(View.GONE);
                nextDirection.setVisibility(GONE);
                v.findViewById(R.id.next).setVisibility(GONE);
            } else {
                v.findViewById(R.id.next).setVisibility(VISIBLE);
                nextManeuver.setVisibility(View.VISIBLE);
                nextDirection.setVisibility(VISIBLE);
                nextDirection.setImageResource(displayHelper
                        .getImageResourceForDirection(context, m.turnManeuver));
                nextManeuver.setText(displayHelper.stringForDirection(
                        context, m.turnManeuver));
            }

            if (position == maneuvers.size() - 1) {
                v.findViewById(R.id.next).setVisibility(GONE);
                final int pointCount = navigator.getRoute().getPoints().size();
                PointOptions finalPoint = navigator.getRoute().getPoints().get(pointCount - 1);
                String customLocation = context.getString(R.string.custom_location_title);
                String arrive = context.getString(R.string.to_arrive,
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
            return view.equals(object);
        }
    }
}
