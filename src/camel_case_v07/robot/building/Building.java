package camel_case_v07.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v07.dijkstra.Dijkstra;
import camel_case_v07.robot.Robot;

public abstract class Building extends Robot {
    public Building(RobotController rc, RobotType type, Dijkstra dijkstra) {
        super(rc, type, dijkstra);
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
