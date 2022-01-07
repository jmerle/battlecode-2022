package camel_case.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SharedArray {
    private RobotController rc;

    public SharedArray(RobotController rc) {
        this.rc = rc;
    }

    public MapLocation getMyArchonLocation(int archonIndex) throws GameActionException {
        int value = rc.readSharedArray(archonIndex);
        return value > 0 ? intToLocation(value) : null;
    }

    public void setMyArchonLocation(int archonIndex, MapLocation location) throws GameActionException {
        write(archonIndex, locationToInt(location));
    }

    public MapLocation getEnemyArchonLocation(int archonIndex) throws GameActionException {
        int value = rc.readSharedArray(archonIndex + 5);
        return value > 0 ? intToLocation(value) : null;
    }

    public void setEnemyArchonLocation(int archonIndex, MapLocation location) throws GameActionException {
        write(archonIndex + 5, location != null ? locationToInt(location) : 0);
    }

    public MapLocation getPossibleEnemyArchonLocation(int index) throws GameActionException {
        int value = rc.readSharedArray(index + 10);
        return value > 0 ? intToLocation(value) : null;
    }

    public void setPossibleEnemyArchonLocation(int index, MapLocation location) throws GameActionException {
        write(index + 10, location != null ? locationToInt(location) : 0);
    }

    public int getArchonTurnIndex() throws GameActionException {
        int value = rc.readSharedArray(25);

        int roundOffset = rc.getRoundNum() * 10;
        if (value < roundOffset) {
            write(25, roundOffset);
            return 0;
        } else {
            write(25, value + 1);
            return value - roundOffset + 1;
        }
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
