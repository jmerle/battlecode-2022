package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class Robot {
    protected RobotController rc;
    protected RobotType me;

    protected Team myTeam;
    protected Team enemyTeam;

    public Robot(RobotController rc, RobotType type) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void run() throws GameActionException {
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        Direction bestDirection = null;
        int minRubble = Integer.MAX_VALUE;
        int minDistance = Integer.MAX_VALUE;

        int currentDistance = target.distanceSquaredTo(myLocation);

        for (Direction possibleDirection : Direction.allDirections()) {
            if (!rc.canMove(possibleDirection)) {
                continue;
            }

            MapLocation newLocation = myLocation.add(possibleDirection);
            int distance = target.distanceSquaredTo(newLocation);
            if (distance > currentDistance) {
                continue;
            }

            int rubble = rc.senseRubble(newLocation);
            if (rubble < minRubble || (rubble == minRubble && distance < minDistance)) {
                bestDirection = possibleDirection;
                minRubble = rubble;
                minDistance = distance;
            }
        }

        if (bestDirection != null) {
            rc.move(bestDirection);
            return true;
        }

        return false;
    }
}
