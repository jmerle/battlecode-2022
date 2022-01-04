package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Builder extends Droid {
    public Builder(RobotController rc) {
        super(rc, RobotType.BUILDER);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
