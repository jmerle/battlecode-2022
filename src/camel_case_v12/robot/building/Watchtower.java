package camel_case_v12.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case_v12.dijkstra.Dijkstra34;
import camel_case_v12.util.ArrayUtils;

public class Watchtower extends Building {
    private int[] possibleEnemyArchonIndices = new int[15];

    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER, new Dijkstra34(rc));

        for (int i = 0; i < 15; i++) {
            possibleEnemyArchonIndices[i] = i;
        }

        ArrayUtils.shuffle(possibleEnemyArchonIndices);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        RobotInfo target = getAttackTarget(me.actionRadiusSquared);
        if (target != null) {
            if (rc.getMode() == RobotMode.PORTABLE) {
                tryTransform();
                return;
            }

            tryAttack(target);
        }

        if (rc.getMode() == RobotMode.TURRET) {
            tryTransform();
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
}
