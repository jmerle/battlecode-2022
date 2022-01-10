package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra20;

public class Soldier extends Droid {
    public Soldier(RobotController rc) {
        super(rc, RobotType.SOLDIER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null) {
            tryAttack(attackTarget);
            return;
        }

        MapLocation dangerTarget = getClosestDangerTarget();
        if (dangerTarget != null) {
            tryMoveTo(dangerTarget);
            return;
        }

        MapLocation archonTarget = getArchonTarget();
        if (archonTarget != null) {
            tryMoveTo(archonTarget);
            return;
        }

        tryWander();
    }
}
