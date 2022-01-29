package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra20;

public class Sage extends Droid {
    public Sage(RobotController rc) {
        super(rc, RobotType.SAGE, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null && visibleTarget.type.canAttack() && !rc.isActionReady()) {
            tryMoveToSafety();
        }

        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null) {
            tryAttack(attackTarget);
            return;
        }

        if (visibleTarget != null && tryMoveToAndAttack(visibleTarget.location)) {
            return;
        }

        if (tryMoveToAndAttack(getClosestDangerTarget())) {
            return;
        }

        if (tryMoveToAndAttack(getArchonTarget())) {
            return;
        }

        if (tryMoveToAndAttack(getPossibleArchonTarget())) {
            return;
        }

        tryWander();
    }
}
