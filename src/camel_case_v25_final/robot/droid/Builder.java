package camel_case_v25_final.robot.droid;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case_v25_final.dijkstra.Dijkstra20;

public class Builder extends Droid {
    private MapLocation archonLocation = null;

    public Builder(RobotController rc) {
        super(rc, RobotType.BUILDER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        if (archonLocation == null) {
            archonLocation = getClosestArchon();
            if (archonLocation == null) {
                return;
            }
        }

        for (Direction direction : adjacentDirections) {
            MapLocation location = rc.adjacentLocation(direction);
            if (!rc.canSenseLocation(location)) {
                continue;
            }

            RobotInfo robot = rc.senseRobotAtLocation(location);
            if (robot != null && robot.team == myTeam && robot.mode == RobotMode.PROTOTYPE) {
                tryRepair(robot.location);
                return;
            }
        }

        Direction bestDirection = null;
        int minDistance = Integer.MAX_VALUE;

        for (Direction direction : adjacentDirections) {
            if (!rc.canBuildRobot(RobotType.WATCHTOWER, direction)) {
                continue;
            }

            MapLocation location = rc.adjacentLocation(direction);
            if (Math.abs(location.x - archonLocation.x) % 2 != 0 || Math.abs(location.y - archonLocation.y) % 2 != 0) {
                continue;
            }

            int distance = location.distanceSquaredTo(archonLocation);
            if (distance < minDistance) {
                bestDirection = direction;
                minDistance = distance;
            }
        }

        if (bestDirection != null) {
            rc.buildRobot(RobotType.WATCHTOWER, bestDirection);
        }

        tryWander();
    }

    private boolean tryRepair(MapLocation location) throws GameActionException {
        if (rc.canRepair(location)) {
            rc.repair(location);
            return true;
        }

        return false;
    }
}
