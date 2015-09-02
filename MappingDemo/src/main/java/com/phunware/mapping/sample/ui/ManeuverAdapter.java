package com.phunware.mapping.sample.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.phunware.mapping.library.maps.PwBuildingMapManager;
import com.phunware.mapping.model.PWRouteManeuver;
import com.phunware.mapping.sample.R;

import java.util.ArrayList;

/**
 * Created by vasudhak on 23/07/15.
 */
public class ManeuverAdapter extends ArrayAdapter<PWRouteManeuver> {

    private Context context;
    private LayoutInflater inflater;
    private PwBuildingMapManager mPwBuildingMapManager;
    private int indexOfDirectionConsumed = 0;
    private ManeuverDisplayHelper maneuverDisplayHelper= new ManeuverDisplayHelper();

    public ManeuverAdapter(Context context, ArrayList<PWRouteManeuver> values, PwBuildingMapManager mPwBuildingMapManager) {
        super(context, 0, values);
        this.context = context;
        this.mPwBuildingMapManager = mPwBuildingMapManager;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(maneuverDisplayHelper!=null)
            maneuverDisplayHelper.setBuilding(mPwBuildingMapManager.getBuilding());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View contentView = convertView;
        PWRouteManeuver maneuver = getItem(position);

        if(contentView == null)
            contentView = inflater.inflate(R.layout.item_maneuver_list, parent, false);

        if(maneuver != null) {
            ((ImageView) contentView.findViewById(R.id.imgDirection)).setImageResource(position == (getCount() - 1) ? R.drawable.arrow_final : maneuverDisplayHelper.getImageResourceForDirection(maneuver));

            if(isConsumed(position)){
                contentView.setBackgroundColor(Color.argb(56, 50, 170, 255));
                ((TextView) contentView.findViewById(R.id.txtDirection)).setText(maneuverDisplayHelper.stringForDirection(maneuver) == null ? "" : maneuverDisplayHelper.stringForDirection(maneuver));
                ((TextView) contentView.findViewById(R.id.txtDirection)).setPaintFlags( ((TextView) contentView.findViewById(R.id.txtDirection)).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );
            } else{
                contentView.setBackgroundColor(Color.argb(255, 255, 255, 255));
                ((TextView) contentView.findViewById(R.id.txtDirection)).setText(maneuverDisplayHelper.stringForDirection(maneuver) == null ? "" : maneuverDisplayHelper.stringForDirection(maneuver));
                ((TextView) contentView.findViewById(R.id.txtDirection)).setPaintFlags(((TextView) contentView.findViewById(R.id.txtDirection)).getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        return contentView;
    }

    private boolean isConsumed(int pos){
        if(pos < indexOfDirectionConsumed)
            return true;

        return false;
    }

    public void increaseIndexOfDirection(){
        indexOfDirectionConsumed = indexOfDirectionConsumed + 2;
        notifyDataSetChanged();
    }

    public void decreaseIndexOfDirection(){
        indexOfDirectionConsumed = indexOfDirectionConsumed - 2;
        notifyDataSetChanged();
    }


}
