package camel_case_v14.robot.droid;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v14.dijkstra.Dijkstra20;

public class Miner extends Droid {
    private MapLocation archonLocation = null;

    public Miner(RobotController rc) {
        super(rc, RobotType.MINER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        if (archonLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type == RobotType.ARCHON) {
                    archonLocation = robot.location;
                    break;
                }
            }
        }

        MapLocation myLocation = rc.getLocation();

        MapLocation closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (MapLocation location : rc.senseNearbyLocationsWithGold(me.visionRadiusSquared)) {
            int distance = myLocation.distanceSquaredTo(location);
            if (distance <= 2) {
                tryMineGold(location);
                return;
            }

            if (distance < minDistance) {
                closestTarget = location;
                minDistance = distance;
            }
        }

        if (closestTarget != null) {
            tryMoveTo(closestTarget);
            return;
        }

        for (MapLocation location : rc.senseNearbyLocationsWithLead(me.visionRadiusSquared)) {
            if (rc.senseLead(location) == 1) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(location);
            if (distance <= 2) {
                tryMineLead(location);
                return;
            }

            if (distance <= minDistance) {
                closestTarget = location;
                minDistance = distance;
            }
        }

        if (closestTarget != null) {
            tryMoveTo(closestTarget);
        } else {
            tryWander();
        }
    }

    private boolean tryMineGold(MapLocation location) throws GameActionException {
        if (!rc.canMineGold(location)) {
            return false;
        }

        while (rc.canMineGold(location)) {
            rc.mineGold(location);
        }

        return true;
    }

    private boolean tryMineLead(MapLocation location) throws GameActionException {
        if (!rc.canMineLead(location) || rc.senseLead(location) <= 1) {
            return false;
        }

        while (rc.canMineLead(location) && rc.senseLead(location) > 1) {
            rc.mineLead(location);
        }

        for (Direction direction : adjacentDirections) {
            tryMineLead(rc.adjacentLocation(direction));
        }

        MapLocation myLocation = rc.getLocation();
        if (archonLocation != null && myLocation.isAdjacentTo(archonLocation)) {
            for (Direction direction : adjacentDirections) {
                if (!myLocation.add(direction).isAdjacentTo(archonLocation) && tryMove(direction)) {
                    break;
                }
            }
        }

        return true;
    }
}
