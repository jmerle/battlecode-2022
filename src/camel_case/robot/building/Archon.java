package camel_case.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon extends Building {
    public Archon(RobotController rc) {
        super(rc, RobotType.ARCHON);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
