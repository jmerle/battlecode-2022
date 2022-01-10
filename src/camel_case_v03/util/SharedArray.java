package camel_case_v03.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SharedArray {
    private RobotController rc;

    public SharedArray(RobotController rc) {
        this.rc = rc;
    }

    public MapLocation getEnemyArchonLocation(int archonIndex) throws GameActionException {
        int value = rc.readSharedArray(archonIndex);
        return value > 0 ? intToLocation(value) : null;
    }

    public void setEnemyArchonLocation(int archonIndex, MapLocation location) throws GameActionException {
        write(archonIndex, location != null ? locationToInt(location) : 0);
    }

    public int archonIdToIndex(int id) {
        return id % 2 == 0 ? id / 2 : (id - 1) / 2;
    }

    private int locationToInt(MapLocation location) {
        return (location.y * 60 + location.x) + 1;
    }

    private MapLocation intToLocation(int n) {
        return new MapLocation((n - 1) % 60, (n - 1) / 60);
    }

    private void write(int index, int newValue) throws GameActionException {
        if (rc.readSharedArray(index) != newValue) {
            rc.writeSharedArray(index, newValue);
        }
    }
}
