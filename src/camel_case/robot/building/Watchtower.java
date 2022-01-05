package camel_case.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra34;

public class Watchtower extends Building {
    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER, new Dijkstra34(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
