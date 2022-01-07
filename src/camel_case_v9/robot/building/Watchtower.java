package camel_case_v9.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v9.dijkstra.Dijkstra34;

public class Watchtower extends Building {
    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER, new Dijkstra34(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        RobotInfo target = getAttackTarget(me.actionRadiusSquared);
        if (target != null) {
            tryAttack(target);
        }
    }
}
