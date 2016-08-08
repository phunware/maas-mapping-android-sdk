package com.phunware.sample.util;

import com.phunware.mapping.model.BuildingOptions;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuverOptions;
import com.phunware.sample.R;

import java.util.List;

/**
 * TODO Taken directly from legacy source.  Needs to be refactored to use localized strings.
 *
 * Created by vkodali on 8/17/15.
 */
public class ManeuverDisplayHelper {


    private BuildingOptions building;
    public BuildingOptions getBuilding() {
        return building;
    }

    public void setBuilding(BuildingOptions building) {
        this.building = building;
    }

    public  String stringForDirection(RouteManeuverOptions maneuver)
    {
        StringBuilder directionString = new StringBuilder();
        switch (maneuver.getDirection()) {
            case FLOOR_CHANGE:
                directionString.append(floorChangeDescriptionForManeuver(maneuver));
                break;
            case BEAR_LEFT:
                directionString.append("Bear Left");
                break;
            case BEAR_RIGHT:
                directionString.append("Bear Right");
                break;
            case LEFT:
                directionString.append("Turn Left");
                break;
            case RIGHT:
                directionString.append("Turn right");
                break;
            case STRAIGHT:
                directionString.append("Continue straight for ");
                directionString.append(getStringDistance(maneuver.getDistance()));
                break;
            default:
                directionString.append("Unknown");
                break;
        }
        return directionString.toString();
    }

    public String  getStringDistance(double distance) {
        double res = distance * 3.28084;
        res = Math.ceil(res);
        return "" + Double.valueOf(res).intValue() + " feet";
    }

    public int getImageResourceForDirection(RouteManeuverOptions  maneuver) {
        int resource = 0;

        switch (maneuver.getDirection()) {
            case STRAIGHT:
                resource = R.drawable.ic_arrow_straight;
                break;
            case LEFT:
                resource = R.drawable.ic_arrow_left;
                break;
            case RIGHT:
                resource = R.drawable.ic_arrow_right;
                break;
            case BEAR_LEFT:
                resource = R.drawable.ic_arrow_bear_left;
                break;
            case BEAR_RIGHT:
                resource = R.drawable.ic_arrow_bear_right;
                break;
            case FLOOR_CHANGE:
                String changeDescription = floorChangeDescriptionForManeuver(maneuver);
                if (changeDescription.toLowerCase().contains("elevator")) {
                    if (changeDescription.toLowerCase().contains("down")) {
                        resource = R.drawable.ic_elevator_down;
                    } else {
                        resource = R.drawable.ic_elevator_up;
                    }
                } else {
                    if (changeDescription.toLowerCase().contains("down")) {
                        resource = R.drawable.ic_stairs_down;
                    } else {
                        resource = R.drawable.ic_stairs_up;
                    }
                }
                break;
        }

        return resource;
    }

    private String floorChangeDescriptionForManeuver(RouteManeuverOptions maneuver)
    {
        PointOptions endPoint = maneuver.getPoints().get(maneuver.getPoints().size() - 1);
        String endPointName = endPoint.getName();
        StringBuilder descriptionString = new StringBuilder();
        if (endPointName != null && endPointName.toLowerCase().contains("elevator"))
        {
            descriptionString.append("Take the elevator");
        }
        else if (endPointName != null && endPointName.toLowerCase().contains("stairs"))
        {
            descriptionString.append("Take the stairs");
        }

        FloorChangeDirection direction = directionForManeuver(maneuver);
        if (direction == FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp)
            descriptionString.append(" up to Level ");
        else if (direction == FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown)
            descriptionString.append(" down to Level ");
        else
            descriptionString.append(" to ");

        descriptionString.append(endPoint.getLevel());

        return descriptionString.toString();
    }

    private FloorChangeDirection directionForManeuver(RouteManeuverOptions maneuver)
    {
        PointOptions startPoint = maneuver.getPoints().get(0);
        PointOptions endPoint = maneuver.getPoints().get(maneuver.getPoints().size() - 1);
        if (startPoint.getLevel() < endPoint.getLevel())
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp;
        else if (startPoint.getLevel() > endPoint.getLevel())
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown;
        else
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionSameFloor;

    }

    private enum FloorChangeDirection {

        PWManeuverDisplayHelperFloorChangeDirectionSameFloor,
        PWManeuverDisplayHelperFloorChangeDirectionUp,
        PWManeuverDisplayHelperFloorChangeDirectionDown;

    }

}
