package camel_case_international_qualifier.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_international_qualifier.dijkstra.Dijkstra;
import camel_case_international_qualifier.robot.Robot;

public abstract class Droid extends Robot {
    public Droid(RobotController rc, RobotType type, Dijkstra dijkstra) {
        super(rc, type, dijkstra);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
