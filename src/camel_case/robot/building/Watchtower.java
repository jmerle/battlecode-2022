package camel_case.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Watchtower extends Building {
    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
