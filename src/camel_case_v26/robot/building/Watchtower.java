package camel_case_v26.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case_v26.dijkstra.Dijkstra34;

public class Watchtower extends Building {
    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER, new Dijkstra34(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null) {
            if (rc.getMode() == RobotMode.PORTABLE) {
                tryTransform();
                return;
            }

            tryAttack(attackTarget);
            return;
        }

        if (rc.getMode() == RobotMode.TURRET) {
            tryTransform();
            return;
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null) {
            tryMoveTo(visibleTarget.location);
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

        MapLocation possibleArchonTarget = getPossibleArchonTarget();
        if (possibleArchonTarget != null) {
            tryMoveTo(possibleArchonTarget);
            return;
        }

        tryWander();
    }
}
