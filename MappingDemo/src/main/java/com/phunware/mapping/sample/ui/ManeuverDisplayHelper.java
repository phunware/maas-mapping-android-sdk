package com.phunware.mapping.sample.ui;

import com.phunware.mapping.model.PWRouteManeuver;
import com.phunware.mapping.model.PwBuilding;
import com.phunware.mapping.model.PwFloor;
import com.phunware.mapping.model.PwPoint;
import com.phunware.mapping.sample.R;

import java.util.List;

/**
 * Created by vkodali on 8/17/15.
 */
public class ManeuverDisplayHelper {


    private PwBuilding building;
    public PwBuilding getBuilding() {
        return building;
    }

    public void setBuilding(PwBuilding building) {
        this.building = building;
    }

    public int getFloorIdMappingString(long floorID)
    {
        List<PwFloor> floors =null;
        if(building!=null) floors = building.getFloors();
        int floorIndex=(int)floorID;
        if(floors!=null) {
            for (final PwFloor pwFloor : floors) {
                if (pwFloor.getId() == floorID) {
                    floorIndex = floors.indexOf(pwFloor);
                    break;
                }
            }
        }
        return floorIndex;
    }

    public String stringForDirection(PWRouteManeuver maneuver)
    {
        StringBuilder directionString=new StringBuilder();
        switch(maneuver.getDirection())
        {
            case PW_ROUTE_MANEUVER_DIRECTION_FLOORCHANGE:
                directionString.append(floorChangeDescriptionForManeuver(maneuver));
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_BEARLEFT: {
                directionString.append("Bear Left for ");
                directionString.append(getStringDistance(maneuver.getDistance()));
                break;
            }
            case PW_ROUTE_MANEUVER_DIRECTION_BEARRIGHT: {

                directionString.append("Bear Right for ");
                directionString.append(getStringDistance(maneuver.getDistance()));
                break;
            }
            case PW_ROUTE_MANEUVER_DIRECTION_LEFT:
                directionString.append("Turn Left");
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_RIGHT:
                directionString.append( "Turn right");
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_STRAIGHT:
                directionString.append("Continue straight for ");
                directionString.append(getStringDistance(maneuver.getDistance()));
                break;
            default:
                directionString.append("Unknown");
                break;
        }
       if(maneuver.getNextManeuver() == null)
       {
           PwPoint endPoint= maneuver.getPoints().get(maneuver.getPoints().size()-1);
           if(endPoint!=null && endPoint.getName()!=null)
           {
               directionString.append(" to arrive at");
               directionString.append(endPoint.getName());
           }
           else
           {
               directionString.append("to arrive at your destination");
           }
       }
        return directionString.toString();
    }

    public String getStringDistance(double distance){
        double res = distance * 3.28084;
        res = Math.ceil(res);
        return "" + res + " feet";
    }
    public int getImageResourceForDirection( PWRouteManeuver maneuver) {
        int resource = 0;

        switch (maneuver.getDirection()) {
            case PW_ROUTE_MANEUVER_DIRECTION_STRAIGHT:
                resource = R.drawable.arrow_straight;
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_LEFT:
                resource = R.drawable.arrow_left;
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_RIGHT:
                resource = R.drawable.arrow_right;
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_BEARLEFT:
                resource = R.drawable.arrow_bear_left;
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_BEARRIGHT:
                resource = R.drawable.arrow_bear_right;
                break;
            case PW_ROUTE_MANEUVER_DIRECTION_FLOORCHANGE: {
                PwPoint point=maneuver.getPoints().get(maneuver.getPoints().size() - 1);
                if(point.getName()!=null && point.getName().toLowerCase().contains("elevator")) {
                    PWRouteManeuverDisplayHelperFloorChangeDirection direction = directionForManeuver(maneuver);
                    if (direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp) {
                        resource = R.drawable.arrow_stairs;
                    } else if (direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown) {
                        resource = R.drawable.arrow_stairs;
                    } else {
                        resource = R.drawable.arrow_stairs;
                    }
                }
                PWRouteManeuverDisplayHelperFloorChangeDirection direction = directionForManeuver(maneuver);
                if (direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp) {
                    resource = R.drawable.arrow_stairs;
                } else if (direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown) {
                    resource = R.drawable.arrow_stairs;
                } else {
                    resource = R.drawable.arrow_stairs;
                }

                break;
            }
        }

        return resource;
    }

    private String floorChangeDescriptionForManeuver(PWRouteManeuver maneuver)
    {
        PwPoint endPoint =maneuver.getPoints().get(maneuver.getPoints().size()-1);
        StringBuilder descriptionString=new StringBuilder();
        if(endPoint.getName()!=null && endPoint.getName().toLowerCase().contains("elevator"))
        {
            descriptionString.append("Take the elevator");
        }
        else if(endPoint.getName()!=null && endPoint.getName().toLowerCase().contains("stairs"))
        {
            descriptionString.append("Take the stairs");
        }

        PWRouteManeuverDisplayHelperFloorChangeDirection direction =directionForManeuver(maneuver);
        if(direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp)
            descriptionString.append(" up to Level ");
        else if(direction == PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown)
            descriptionString.append(" down to Level");
        else
            descriptionString.append(" to ");

        descriptionString.append(getFloorIdMappingString(endPoint.getFloorId()));

        return descriptionString.toString();
    }


    private PWRouteManeuverDisplayHelperFloorChangeDirection directionForManeuver(PWRouteManeuver maneuver)
    {
        PwPoint startPoint= maneuver.getPoints().get(0);
        PwPoint endPoint =maneuver.getPoints().get(maneuver.getPoints().size()-1);
        if(getFloorIdMappingString(startPoint.getFloorId())<getFloorIdMappingString(endPoint.getFloorId()))
            return PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp;
        else if (getFloorIdMappingString(startPoint.getFloorId())>getFloorIdMappingString(endPoint.getFloorId()))
            return PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown;
        else
        return PWRouteManeuverDisplayHelperFloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionSameFloor;

    }

    public enum PWRouteManeuverDisplayHelperFloorChangeDirection {

        PWManeuverDisplayHelperFloorChangeDirectionSameFloor,
        PWManeuverDisplayHelperFloorChangeDirectionUp,
        PWManeuverDisplayHelperFloorChangeDirectionDown;

    };


}
