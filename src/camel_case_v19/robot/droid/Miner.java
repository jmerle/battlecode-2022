package camel_case_v19.robot.droid;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v19.dijkstra.Dijkstra20;
import camel_case_v19.util.BattlecodeFunction;

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
        if (archonLocation != null && myLocation.isAdjacentTo(archonLocation)) {
            for (Direction direction : adjacentDirections) {
                if (!rc.adjacentLocation(direction).isAdjacentTo(archonLocation) && tryMove(direction)) {
                    break;
                }
            }
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null && visibleTarget.type.canAttack()) {
            tryMoveAway(visibleTarget.location);
            tryMineLeadAllDirections();
            return;
        }

        if (tryMine(rc.senseNearbyLocationsWithGold(), this::senseGold, this::tryMineGold)) {
            tryMineLeadAllDirections();
            return;
        }

        if (tryMine(rc.senseNearbyLocationsWithLead(2, 2), this::senseLead, this::tryMineLead)) {
            tryMineLeadAllDirections();
            return;
        }

        if (tryMine(rc.senseNearbyLocationsWithLead(me.visionRadiusSquared, 2), this::senseLead, this::tryMineLead)) {
            tryMineLeadAllDirections();
            return;
        }

        tryWander();
        tryMineLeadAllDirections();
    }

    private boolean tryMine(MapLocation[] options,
                            BattlecodeFunction<MapLocation, Integer> senseResources,
                            BattlecodeFunction<MapLocation, Boolean> tryMine) throws GameActionException {
        if (options.length == 0) {
            return false;
        }

        MapLocation myLocation = rc.getLocation();

        MapLocation bestOption = null;
        int maxResources = Integer.MIN_VALUE;

        for (MapLocation option : options) {
            if (option.equals(myLocation)) {
                tryMine.apply(option);
                return true;
            }

            int resources = senseResources.apply(option);
            if (resources > maxResources) {
                bestOption = option;
                maxResources = resources;
            }
        }

        if (bestOption != null) {
            if (archonLocation == null || !bestOption.isAdjacentTo(archonLocation) || !myLocation.isAdjacentTo(bestOption)) {
                tryMoveTo(bestOption);
            }

            tryMine.apply(bestOption);
            return true;
        }

        return false;
    }

    private int senseGold(MapLocation location) throws GameActionException {
        return rc.senseGold(location);
    }

    private boolean tryMineGold(MapLocation location) throws GameActionException {
        if (!rc.canMineGold(location)) {
            return false;
        }

        while (rc.canMineGold(location)) {
            rc.mineGold(location);
        }

        for (Direction direction : adjacentDirections) {
            MapLocation adjacentLocation = rc.adjacentLocation(direction);
            while (rc.canMineGold(adjacentLocation)) {
                rc.mineGold(adjacentLocation);
            }
        }

        return true;
    }

    private int senseLead(MapLocation location) throws GameActionException {
        return rc.senseLead(location);
    }

    private boolean tryMineLead(MapLocation location) throws GameActionException {
        if (!rc.canMineLead(location)) {
            return false;
        }

        while (rc.canMineLead(location) && rc.senseLead(location) > 1) {
            rc.mineLead(location);
        }

        for (Direction direction : adjacentDirections) {
            MapLocation adjacentLocation = rc.adjacentLocation(direction);
            while (rc.canMineLead(adjacentLocation) && rc.senseLead(adjacentLocation) > 1) {
                rc.mineLead(adjacentLocation);
            }
        }

        return true;
    }

    private void tryMineLeadAllDirections() throws GameActionException {
        for (Direction direction : adjacentDirections) {
            MapLocation location = rc.adjacentLocation(direction);
            while (rc.canMineLead(location) && rc.senseLead(location) > 1) {
                rc.mineLead(location);
            }
        }
    }
}
