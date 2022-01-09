package camel_case_v12.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SharedArray {
    public static final int MAX_DANGER_TARGETS = 20;

    private RobotController rc;

    public SharedArray(RobotController rc) {
        this.rc = rc;
    }

    public MapLocation getMyArchonLocation(int archonIndex) throws GameActionException {
        return readLocation(archonIndex);
    }

    public void setMyArchonLocation(int archonIndex, MapLocation location) throws GameActionException {
        writeLocation(archonIndex, location);
    }

    public MapLocation getEnemyArchonLocation(int archonIndex) throws GameActionException {
        return readLocation(archonIndex + 5);
    }

    public void setEnemyArchonLocation(int archonIndex, MapLocation location) throws GameActionException {
        writeLocation(archonIndex + 5, location);
    }

    public MapLocation getPossibleEnemyArchonLocation(int index) throws GameActionException {
        return readLocation(index + 10);
    }

    public void setPossibleEnemyArchonLocation(int index, MapLocation location) throws GameActionException {
        writeLocation(index + 10, location);
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

    public MapLocation getDangerTarget(int index) throws GameActionException {
        return readLocation(index + 26);
    }

    public void setDangerTarget(int index, MapLocation location) throws GameActionException {
        writeLocation(index + 26, location);
    }

    public int archonIdToIndex(int id) {
        return id % 2 == 0 ? id / 2 : (id - 1) / 2;
    }

    private MapLocation readLocation(int index) throws GameActionException {
        int value = rc.readSharedArray(index);
        return value > 0 ? new MapLocation((value - 1) % 60, (value - 1) / 60) : null;
    }

    private void writeLocation(int index, MapLocation location) throws GameActionException {
        write(index, location != null ? (location.y * 60 + location.x) + 1 : 0);
    }

    private void write(int index, int newValue) throws GameActionException {
        if (rc.readSharedArray(index) != newValue) {
            rc.writeSharedArray(index, newValue);
        }
    }
}
