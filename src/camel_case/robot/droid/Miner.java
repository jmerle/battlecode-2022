package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra20;

public class Miner extends Droid {
    public Miner(RobotController rc) {
        super(rc, RobotType.MINER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();
    }
}
