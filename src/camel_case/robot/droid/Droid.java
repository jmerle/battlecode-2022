package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.robot.Robot;

public abstract class Droid extends Robot {
    public Droid(RobotController rc, RobotType type) {
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
