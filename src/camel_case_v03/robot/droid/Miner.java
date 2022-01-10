package camel_case_v03.robot.droid;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v03.dijkstra.Dijkstra20;

public class Miner extends Droid {
    public Miner(RobotController rc) {
        super(rc, RobotType.MINER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        MapLocation myLocation = rc.getLocation();

        MapLocation mineGoldTarget = null;
        MapLocation moveGoldTarget = null;
        int moveGoldTargetDistance = Integer.MAX_VALUE;

        MapLocation mineLeadTarget = null;
        MapLocation moveLeadTarget = null;
        int moveLeadTargetDistance = Integer.MAX_VALUE;

        for (MapLocation location : rc.senseNearbyLocationsWithGold(me.visionRadiusSquared)) {
            int distance = myLocation.distanceSquaredTo(location);
            if (distance <= 2) {
                mineGoldTarget = location;
            } else if (distance < moveGoldTargetDistance) {
                moveGoldTarget = location;
                moveGoldTargetDistance = distance;
            }
        }

        for (MapLocation location : rc.senseNearbyLocationsWithLead(me.visionRadiusSquared)) {
            if (rc.senseLead(location) == 1) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(location);
            if (distance <= 2) {
                mineLeadTarget = location;
            } else if (distance <= moveLeadTargetDistance) {
                moveLeadTarget = location;
                moveLeadTargetDistance = distance;
            }
        }

        if (mineGoldTarget != null) {
            if (myLocation.equals(mineGoldTarget)) {
                tryMoveRandom();
            } else {
                tryMineGold(mineGoldTarget);
            }
        } else if (moveGoldTarget != null) {
            tryMoveTo(moveGoldTarget);
        } else if (mineLeadTarget != null) {
            if (myLocation.equals(mineLeadTarget)) {
                tryMoveRandom();
            } else {
                tryMineLead(mineLeadTarget);
            }
        } else if (moveLeadTarget != null) {
            tryMoveTo(moveLeadTarget);
        } else {
            tryWander();
        }
    }

    private boolean tryMineGold(MapLocation location) throws GameActionException {
        boolean minedGold = false;
        while (rc.canMineGold(location)) {
            rc.mineGold(location);
            minedGold = true;
        }

        return minedGold;
    }

    private boolean tryMineLead(MapLocation location) throws GameActionException {
        boolean minedLead = false;
        while (rc.canMineLead(location) && rc.senseLead(location) > 1) {
            rc.mineLead(location);
            minedLead = true;
        }

        return minedLead;
    }
}
