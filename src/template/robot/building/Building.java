package template.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import template.robot.Robot;

public abstract class Building extends Robot {
    public Building(RobotController rc, RobotType type) {
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
