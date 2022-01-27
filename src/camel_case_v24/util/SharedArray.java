package camel_case_v24.util;

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
        int value = rc.readSharedArray(index + 26);
        return value > 0 ? intToLocation(value % 5_000) : null;
    }

    public void setDangerTarget(int index, MapLocation location, int expiration) throws GameActionException {
        write(index + 26, locationToInt(location) + expiration * 5_000);
    }

    public void addDangerTarget(MapLocation location, int expiration) throws GameActionException {
        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation dangerTarget = getDangerTarget(i);
            if (dangerTarget == null || dangerTarget.equals(location)) {
                write(i + 26, locationToInt(location) + expiration * 5_000);
                break;
            }
        }
    }

    public void expireDangerTargets() throws GameActionException {
        for (int i = 0; i < MAX_DANGER_TARGETS; i++) {
            int value = rc.readSharedArray(i + 26);
            if (value > 5_000) {
                write(i + 26, value - 5_000);
            } else if (value > 0) {
                write(i + 26, 0);
            }
        }
    }

    public int archonIdToIndex(int id) {
        return id % 2 == 0 ? id / 2 : (id - 1) / 2;
    }

    private int locationToInt(MapLocation location) {
        return (location.y * 60 + location.x) + 1;
    }

    private MapLocation intToLocation(int value) {
        return new MapLocation((value - 1) % 60, (value - 1) / 60);
    }

    private MapLocation readLocation(int index) throws GameActionException {
        int value = rc.readSharedArray(index);
        return value > 0 ? intToLocation(value) : null;
    }

    private void writeLocation(int index, MapLocation location) throws GameActionException {
        write(index, location != null ? locationToInt(location) : 0);
    }

    private void write(int index, int newValue) throws GameActionException {
        if (rc.readSharedArray(index) != newValue) {
            rc.writeSharedArray(index, newValue);
        }
    }
}
