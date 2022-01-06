package camel_case_v4.dijkstra;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface Dijkstra {
    Direction getBestDirection(MapLocation target) throws GameActionException;
}
