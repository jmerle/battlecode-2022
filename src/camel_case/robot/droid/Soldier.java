package camel_case.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra20;
import camel_case.util.ArrayUtils;

public class Soldier extends Droid {
    private int[] possibleEnemyArchonIndices = new int[15];

    public Soldier(RobotController rc) {
        super(rc, RobotType.SOLDIER, new Dijkstra20(rc));

        for (int i = 0; i < 15; i++) {
            possibleEnemyArchonIndices[i] = i;
        }

        ArrayUtils.shuffle(possibleEnemyArchonIndices);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        removeInvalidDangerTargets();
        lookForDangerTargets();

        RobotInfo targetInRange = getAttackTarget(me.actionRadiusSquared);
        if (targetInRange != null) {
            tryAttack(targetInRange);
            return;
        }

        MapLocation dangerTarget = getClosestDangerTarget();
        if (dangerTarget != null) {
            tryMoveTo(dangerTarget);
            return;
        }

        MapLocation myLocation = rc.getLocation();
        for (int i = 0; i < 5; i++) {
            MapLocation archon = sharedArray.getEnemyArchonLocation(i);
            if (archon != null) {
                if (myLocation.distanceSquaredTo(archon) <= me.actionRadiusSquared) {
                    sharedArray.setEnemyArchonLocation(i, null);
                } else {
                    tryMoveTo(archon);
                    return;
                }
            }
        }

        for (int index : possibleEnemyArchonIndices) {
            MapLocation possibleEnemyArchon = sharedArray.getPossibleEnemyArchonLocation(index);
            if (possibleEnemyArchon == null) {
                continue;
            }

            if (rc.canSenseLocation(possibleEnemyArchon)) {
                sharedArray.setPossibleEnemyArchonLocation(index, null);
                continue;
            }

            tryMoveTo(possibleEnemyArchon);
            return;
        }

        tryWander();
    }

    private MapLocation getClosestDangerTarget() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation closestTarget = null;
        int minDangerDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 20; i++) {
            MapLocation target = sharedArray.getDangerTarget(i);
            if (target == null) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(target);
            if (distance < minDangerDistance) {
                closestTarget = target;
                minDangerDistance = distance;
            }
        }

        return closestTarget;
    }
}
