package camel_case_v05.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v05.dijkstra.Dijkstra53;

public class Laboratory extends Building {
    public Laboratory(RobotController rc) {
        super(rc, RobotType.LABORATORY, new Dijkstra53(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
