package camel_case.robot.building;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra34;
import camel_case.robot.droid.Soldier;

public class Watchtower extends Building {
    private Soldier soldier;

    public Watchtower(RobotController rc) {
        super(rc, RobotType.WATCHTOWER, new Dijkstra34(rc));

        soldier = new Soldier(rc);
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

        soldier.run();
    }
}
