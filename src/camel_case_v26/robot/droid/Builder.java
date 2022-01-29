package camel_case_v26.robot.droid;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case_v26.dijkstra.Dijkstra20;

public class Builder extends Droid {
    private MapLocation archonLocation = null;

    private boolean isLaboratoryBuilder = false;
    private MapLocation borderLocation = null;
    private int labsBuilt = 0;
    private int labStartRound = 0;

    public Builder(RobotController rc) {
        super(rc, RobotType.BUILDER, new Dijkstra20(rc));
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        lookForDangerTargets();

        if (!isLaboratoryBuilder) {
            isLaboratoryBuilder = !sharedArray.laboratoryBuilderAlive();
            if (isLaboratoryBuilder) {
                labStartRound = rc.getRoundNum() - 1;
            }
        }

        if (isLaboratoryBuilder) {
            sharedArray.markLaboratoryBuilderAlive();
        }

        if (tryRepairNearby()) {
            return;
        }

        if (borderLocation == null) {
            MapLocation myLocation = rc.getLocation();
            MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);

            /*switch (directionBetween(center, myLocation)) {
                case NORTHEAST:
                    borderLocation = new MapLocation(mapWidth - 1, mapHeight - 1);
                    break;
                case SOUTHEAST:
                    borderLocation = new MapLocation(mapWidth - 1, 0);
                    break;
                case SOUTHWEST:
                    borderLocation = new MapLocation(0, 0);
                    break;
                case NORTHWEST:
                default:
                    borderLocation = new MapLocation(0, mapHeight - 1);
            }*/

            switch (center.directionTo(myLocation)) {
                case NORTH:
                    borderLocation = new MapLocation(mapWidth / 2, mapHeight - 1);
                    break;
                case NORTHEAST:
                    borderLocation = new MapLocation(mapWidth - 1, mapHeight - 1);
                    break;
                case EAST:
                    borderLocation = new MapLocation(mapWidth - 1, mapHeight / 2);
                    break;
                case SOUTHEAST:
                    borderLocation = new MapLocation(mapWidth - 1, 0);
                    break;
                case SOUTH:
                    borderLocation = new MapLocation(mapWidth / 2, 0);
                    break;
                case SOUTHWEST:
                    borderLocation = new MapLocation(0, 0);
                    break;
                case WEST:
                    borderLocation = new MapLocation(0, mapHeight / 2);
                    break;
                case NORTHWEST:
                default:
                    borderLocation = new MapLocation(0, mapHeight - 1);
            }
        }

        if (isLaboratoryBuilder) {
            runLaboratoryBuilder();
        } else {
            runWatchtowerBuilder();
        }
    }

    private void runLaboratoryBuilder() throws GameActionException {
        if (!rc.isActionReady() || rc.getLocation().distanceSquaredTo(borderLocation) > 30) {
            tryWanderSafe();
            return;
        }

        MapLocation archon = getClosestArchon();

        Direction bestDirection = null;
        int minRubble = Integer.MAX_VALUE;
        int minDistance = Integer.MAX_VALUE;

        for (Direction direction : adjacentDirections) {
            MapLocation location = rc.adjacentLocation(direction);
            if (!rc.onTheMap(location)) {
                continue;
            }

            if (rc.senseRobotAtLocation(location) != null) {
                continue;
            }

            if (archon != null && archon.isAdjacentTo(location)) {
                continue;
            }

            int rubble = rc.senseRubble(location);
            if (rubble >= 50) {
                continue;
            }

            int distance = location.distanceSquaredTo(borderLocation);
            if (rubble < minRubble || (rubble == minRubble && distance < minDistance)) {
                bestDirection = direction;
                minRubble = rubble;
                minDistance = distance;
            }
        }

        if (bestDirection != null) {
            if (rc.canBuildRobot(RobotType.LABORATORY, bestDirection)) {
                rc.buildRobot(RobotType.LABORATORY, bestDirection);
                labsBuilt++;
            } else {
                if (labsBuilt < Math.ceil(((double) rc.getRoundNum() - labStartRound) / 300.0)) {
                    sharedArray.markBuilderNeedsResources();
                }

                tryWanderSafe();
            }
        } else {
            tryWanderSafe();
        }
    }

    private void runWatchtowerBuilder() throws GameActionException {
        if (!rc.isActionReady() || rc.getTeamLeadAmount(myTeam) < 300 || rc.getRoundNum() < 300) {
            tryWander();
            return;
        }

        if (archonLocation == null) {
            archonLocation = getClosestArchon();
            if (archonLocation == null) {
                return;
            }
        }

        for (Direction direction : adjacentDirections) {
            MapLocation location = rc.adjacentLocation(direction);
            if (!rc.canSenseLocation(location)) {
                continue;
            }

            RobotInfo robot = rc.senseRobotAtLocation(location);
            if (robot != null && robot.team == myTeam && robot.mode == RobotMode.PROTOTYPE) {
                tryRepair(robot.location);
                return;
            }
        }

        Direction bestDirection = null;
        int minDistance = Integer.MAX_VALUE;

        for (Direction direction : adjacentDirections) {
            if (!rc.canBuildRobot(RobotType.WATCHTOWER, direction)) {
                continue;
            }

            MapLocation location = rc.adjacentLocation(direction);
            if (Math.abs(location.x - archonLocation.x) % 2 != 0 || Math.abs(location.y - archonLocation.y) % 2 != 0) {
                continue;
            }

            int distance = location.distanceSquaredTo(archonLocation);
            if (distance < minDistance) {
                bestDirection = direction;
                minDistance = distance;
            }
        }

        if (bestDirection != null) {
            rc.buildRobot(RobotType.WATCHTOWER, bestDirection);
        }

        tryWander();
    }

    private boolean tryRepair(MapLocation location) throws GameActionException {
        if (rc.canRepair(location)) {
            Direction bestDirection = null;
            int minRubble = rc.senseRubble(rc.getLocation());

            for (Direction direction : adjacentDirections) {
                if (!rc.canMove(direction)) {
                    continue;
                }

                MapLocation newLocation = rc.adjacentLocation(direction);
                if (newLocation.distanceSquaredTo(location) > me.actionRadiusSquared) {
                    continue;
                }

                int rubble = rc.senseRubble(newLocation);
                if (rubble < minRubble) {
                    bestDirection = direction;
                    minRubble = rubble;
                }
            }

            if (bestDirection != null) {
                tryMove(bestDirection);
            }

            rc.repair(location);
            return true;
        }

        return false;
    }

    private boolean tryRepairNearby() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        RobotInfo repairTarget = null;
        int minDistance = Integer.MAX_VALUE;
        int minHealth = Integer.MAX_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
            if (!robot.type.isBuilding() || robot.health == robot.type.getMaxHealth(robot.level)) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(robot.location);
            if (distance < minDistance || (distance == minDistance && robot.health < minHealth)) {
                repairTarget = robot;
                minDistance = distance;
                minHealth = robot.health;
            }
        }

        if (repairTarget != null) {
            if (minDistance <= me.actionRadiusSquared) {
                tryRepair(repairTarget.location);
            } else {
                tryMoveTo(repairTarget.location);
            }

            return true;
        }

        return false;
    }

    private void tryWanderSafe() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(borderLocation) < 20) {
            tryMoveRandom();
        } else {
            tryMoveTo(borderLocation);
        }
    }
}
