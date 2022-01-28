package camel_case_v25_final.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v25_final.dijkstra.Dijkstra20;

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
