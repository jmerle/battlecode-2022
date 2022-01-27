package camel_case_v23.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v23.dijkstra.Dijkstra20;

public class Sage extends Droid {
    public Sage(RobotController rc) {
        super(rc, RobotType.SAGE, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        tryMoveRandom();
    }
}
