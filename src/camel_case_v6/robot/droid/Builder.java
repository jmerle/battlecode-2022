package camel_case_v6.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v6.dijkstra.Dijkstra20;

public class Builder extends Droid {
    public Builder(RobotController rc) {
        super(rc, RobotType.BUILDER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        tryMoveRandom();
    }
}
