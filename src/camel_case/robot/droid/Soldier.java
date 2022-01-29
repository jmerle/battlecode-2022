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

        int myHealth = rc.getHealth();
        if (myHealth < 10 || (myHealth < 16 && distanceToArchon() < 34)) {
            tryMoveToArchon();
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null
                && visibleTarget.type.canAttack()
                && !rc.isActionReady()
                && rc.getHealth() != me.getMaxHealth(rc.getLevel())) {
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

    private int distanceToArchon() throws GameActionException {
        MapLocation archon = getClosestArchon();
        return archon != null ? rc.getLocation().distanceSquaredTo(archon) : Integer.MAX_VALUE;
    }
}
