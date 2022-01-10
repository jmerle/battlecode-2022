package camel_case_v16.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v16.dijkstra.Dijkstra20;

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

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null) {
            tryMoveTo(visibleTarget.location);
            tryAttack(visibleTarget);
            return;
        }

        MapLocation dangerTarget = getClosestDangerTarget();
        if (dangerTarget != null) {
            tryMoveTo(dangerTarget);

            if (rc.canSenseLocation(dangerTarget)) {
                RobotInfo robot = rc.senseRobotAtLocation(dangerTarget);
                if (robot != null) {
                    tryAttack(robot);
                }
            }

            return;
        }

        MapLocation archonTarget = getArchonTarget();
        if (archonTarget != null) {
            tryMoveTo(archonTarget);

            if (rc.canSenseLocation(archonTarget)) {
                RobotInfo robot = rc.senseRobotAtLocation(archonTarget);
                if (robot != null) {
                    tryAttack(robot);
                }
            }

            return;
        }

        tryWander();
    }
}
