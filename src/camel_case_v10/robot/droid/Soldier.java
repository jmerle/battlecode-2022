package camel_case_v10.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v10.dijkstra.Dijkstra20;
import camel_case_v10.util.ArrayUtils;

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

        RobotInfo target = getAttackTarget(me.actionRadiusSquared);
        if (target != null) {
            tryAttack(target);
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

        if (rc.getID() % 3 == 0 && rc.getTeamLeadAmount(myTeam) < 500) {
            tryWander();
            return;
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
