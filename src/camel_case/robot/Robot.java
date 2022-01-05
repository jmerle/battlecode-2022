package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case.dijkstra.Dijkstra;

public abstract class Robot {
    protected RobotController rc;
    protected RobotType me;

    protected Team myTeam;
    protected Team enemyTeam;

    protected Dijkstra dijkstra;

    public Robot(RobotController rc, RobotType type, Dijkstra dijkstra) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        this.dijkstra = dijkstra;
    }

    public void run() throws GameActionException {
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        Direction bestDirection = dijkstra.getBestDirection(target);
        if (bestDirection != null && rc.canMove(bestDirection)) {
            rc.move(bestDirection);
            return true;
        }

        return false;
    }
}
