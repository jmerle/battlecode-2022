package camel_case_v17.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v17.dijkstra.Dijkstra20;

public class Soldier extends Droid {
    public Soldier(RobotController rc) {
        super(rc, RobotType.SOLDIER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        if (rc.getHealth() < 10) {
            tryMoveToArchon();
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null && visibleTarget.type.canAttack() && !rc.isActionReady()) {
            tryMoveAway(visibleTarget.location);
            return;
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

    private boolean tryMoveToAndAttack(MapLocation location) throws GameActionException {
        if (location == null) {
            return false;
        }

        tryMoveTo(location);

        if (rc.canSenseLocation(location)) {
            RobotInfo robot = rc.senseRobotAtLocation(location);
            if (robot != null) {
                tryAttack(robot);
            }
        }

        return true;
    }
}
