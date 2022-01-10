package camel_case_v01.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v01.dijkstra.Dijkstra20;

public class Soldier extends Droid {
    public Soldier(RobotController rc) {
        super(rc, RobotType.SOLDIER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        RobotInfo target = getAttackTarget(me.actionRadiusSquared);
        if (target != null) {
            tryAttack(target.location);
        } else {
            tryWander();
        }
    }
}
