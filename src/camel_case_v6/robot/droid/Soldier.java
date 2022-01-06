package camel_case_v6.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v6.dijkstra.Dijkstra20;

public class Soldier extends Droid {
    public Soldier(RobotController rc) {
        super(rc, RobotType.SOLDIER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        RobotInfo target = getAttackTarget(me.actionRadiusSquared);
        if (target != null) {
            tryAttack(target);
            return;
        }

        for (int i = 0; i < 4; i++) {
            MapLocation archon = sharedArray.getEnemyArchonLocation(i);
            if (archon != null) {
                if (rc.getLocation().distanceSquaredTo(archon) <= me.actionRadiusSquared) {
                    sharedArray.setEnemyArchonLocation(i, null);
                } else {
                    tryMoveTo(archon);
                    return;
                }
            }
        }

        tryWander();
    }
}
