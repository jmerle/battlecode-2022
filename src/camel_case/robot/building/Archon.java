package camel_case.robot.building;

import battlecode.common.AnomalyScheduleEntry;
import battlecode.common.AnomalyType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case.dijkstra.Dijkstra34;
import camel_case.util.RandomUtils;
import camel_case.util.SharedArray;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private boolean isFirstRun = true;

    private MapLocation optimalLocation = null;
    private boolean hasFoundInitialOptimalLocation = false;

    private AnomalyScheduleEntry[] anomalySchedule;

    private int minersSpawned = 0;
    private int maxLeadingMiners;

    private RobotType[] spawnOrder = {
            RobotType.BUILDER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER
    };

    private int spawnOrderIndex = 0;

    public Archon(RobotController rc) {
        super(rc, RobotType.ARCHON, new Dijkstra34(rc));

        setSpawnDirections();
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        int turnIndex = sharedArray.getArchonTurnIndex();
        int archonCount = rc.getArchonCount();

        if (isFirstRun) {
            sharedArray.setMyArchonLocation(sharedArray.archonIdToIndex(rc.getID()), rc.getLocation());

            int knownArchonLocations = 0;
            for (int i = 0; i < 5; i++) {
                if (sharedArray.getMyArchonLocation(i) != null) {
                    knownArchonLocations++;
                }
            }

            if (knownArchonLocations == rc.getArchonCount()) {
                setPossibleEnemyArchonLocations();
            }

            maxLeadingMiners = Math.max(rc.senseNearbyLocationsWithLead(2).length, 5);

            isFirstRun = false;
        }

        if (turnIndex == 0) {
            sharedArray.expireDangerTargets();
        }

        lookForDangerTargets();

        if (rc.getMode() == RobotMode.PORTABLE) {
            if (getAttackTarget(me.visionRadiusSquared) != null) {
                tryTransform();
            } else {
                tryMoveToOptimalLocation();
            }

            return;
        }

        boolean checkForInitialOptimalLocation = false;
        if (!hasFoundInitialOptimalLocation) {
            int currentRound = rc.getRoundNum();
            if (currentRound == 150 || (currentRound == 10 && rc.senseRubble(rc.getLocation()) >= 30)) {
                checkForInitialOptimalLocation = true;
                hasFoundInitialOptimalLocation = true;
            }
        }

        if (checkForInitialOptimalLocation || vortexHappened()) {
            findOptimalLocation();
        }

        RobotInfo attackTarget = getAttackTarget(me.visionRadiusSquared);
        if (attackTarget != null && attackTarget.type.canAttack()) {
            tryBuildRobot(RobotType.SOLDIER);
            tryBuildRobot(RobotType.SAGE);
            tryRepair();
            return;
        }

        if (sharedArray.builderNeedsResources()) {
            tryBuildRobot(RobotType.SAGE);
            tryMoveToOptimalLocation();
            tryRepair();
            return;
        }

        if (sharedArray.laboratoryBuilderAlive() && sharedArray.needMiners()) {
            tryBuildRobot(RobotType.MINER);
            tryMoveToOptimalLocation();
            tryRepair();
            return;
        }

        if (rc.getRoundNum() > 1 && !RandomUtils.chance(((double) turnIndex + 1) / (double) archonCount)) {
            tryBuildRobot(RobotType.SAGE);
            tryMoveToOptimalLocation();
            tryRepair();
            return;
        }

        boolean hasDangerTargets = false;
        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            if (sharedArray.getDangerTarget(i) != null) {
                hasDangerTargets = true;
                break;
            }
        }

        if (!hasDangerTargets && minersSpawned < maxLeadingMiners) {
            if (tryBuildRobot(RobotType.MINER)) {
                minersSpawned++;
            }

            tryBuildRobot(RobotType.SAGE);
            tryRepair();
            return;
        }

        while (spawnOrder[spawnOrderIndex] == RobotType.BUILDER && (turnIndex != 0 || sharedArray.laboratoryBuilderAlive())) {
            spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
        }

        if (tryBuildRobot(spawnOrder[spawnOrderIndex])) {
            spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
        }

        tryBuildRobot(RobotType.SAGE);
        tryMoveToOptimalLocation();
        tryRepair();
    }

    private void setSpawnDirections() {
        spawnDirections = new Direction[8];

        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        spawnDirections[0] = rc.getLocation().directionTo(center);

        spawnDirections[1] = spawnDirections[0].rotateLeft();
        spawnDirections[2] = spawnDirections[0].rotateRight();

        spawnDirections[3] = spawnDirections[1].rotateLeft();
        spawnDirections[4] = spawnDirections[2].rotateRight();

        spawnDirections[5] = spawnDirections[3].rotateLeft();
        spawnDirections[6] = spawnDirections[4].rotateRight();

        spawnDirections[7] = spawnDirections[0].opposite();
    }

    private void setPossibleEnemyArchonLocations() throws GameActionException {
        MapLocation[] locations = new MapLocation[15];

        for (int i = 0; i < 5; i++) {
            MapLocation myArchon = sharedArray.getMyArchonLocation(i);
            if (myArchon == null) {
                continue;
            }

            locations[3 * i] = new MapLocation(mapWidth - myArchon.x - 1, myArchon.y);
            locations[3 * i + 1] = new MapLocation(myArchon.x, mapHeight - myArchon.y - 1);
            locations[3 * i + 2] = new MapLocation(mapWidth - myArchon.x - 1, mapHeight - myArchon.y - 1);
        }

        for (int i = 0; i < 5; i++) {
            MapLocation myArchon = sharedArray.getMyArchonLocation(i);
            if (myArchon == null) {
                continue;
            }

            for (int j = 0; j < 15; j++) {
                if (myArchon.equals(locations[j])) {
                    locations[j] = null;
                    break;
                }
            }
        }

        for (int i = 0; i < 15; i++) {
            if (locations[i] != null) {
                sharedArray.setPossibleEnemyArchonLocation(i, locations[i]);
            }
        }
    }

    private boolean tryBuildRobot(RobotType type) throws GameActionException {
        Direction bestDirection = null;
        int minRubble = Integer.MAX_VALUE;

        for (Direction direction : spawnDirections) {
            if (rc.canBuildRobot(type, direction)) {
                int rubble = rc.senseRubble(rc.adjacentLocation(direction));
                if (rubble < minRubble) {
                    minRubble = rubble;
                    bestDirection = direction;
                }
            }
        }

        if (bestDirection != null && rc.canBuildRobot(type, bestDirection)) {
            rc.buildRobot(type, bestDirection);
            return true;
        }

        return false;
    }

    private void tryRepair() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        RobotInfo bestTarget = null;
        int minHealth = Integer.MAX_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(me.actionRadiusSquared, myTeam)) {
            if (!rc.canRepair(robot.location)
                    || robot.type.isBuilding()
                    || !robot.type.canAttack()
                    || robot.health == robot.type.getMaxHealth(robot.level)) {
                continue;
            }

            if (robot.health < minHealth) {
                bestTarget = robot;
                minHealth = robot.health;
            }
        }

        if (bestTarget != null && rc.canRepair(bestTarget.location)) {
            rc.repair(bestTarget.location);
        }
    }

    private void findOptimalLocation() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        int minArchonRubble = rc.senseRubble(myLocation);
        double minSpawnRubble = getSpawnRubble(myLocation);
        int minDistance = 0;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 20)) {
            int archonRubble = rc.senseRubble(location);
            if (archonRubble > minArchonRubble) {
                continue;
            }

            if (myLocation.equals(location) || rc.senseRobotAtLocation(location) != null) {
                continue;
            }

            double spawnRubble = getSpawnRubble(location);
            int distance = myLocation.distanceSquaredTo(location);

            if (archonRubble < minArchonRubble
                    || spawnRubble < minSpawnRubble
                    || (spawnRubble == minSpawnRubble && distance < minDistance)) {
                optimalLocation = location;
                minArchonRubble = archonRubble;
                minSpawnRubble = spawnRubble;
                minDistance = distance;
            }
        }
    }

    private boolean vortexHappened() {
        if (anomalySchedule == null) {
            anomalySchedule = rc.getAnomalySchedule();
        }

        int round = rc.getRoundNum() - 1;
        for (AnomalyScheduleEntry entry : anomalySchedule) {
            if (entry.roundNumber == round && entry.anomalyType == AnomalyType.VORTEX) {
                return true;
            }
        }

        return false;
    }

    private void tryMoveToOptimalLocation() throws GameActionException {
        if (optimalLocation == null) {
            return;
        }

        if (rc.getLocation().equals(optimalLocation)) {
            if (rc.getMode() == RobotMode.PORTABLE) {
                tryTransform();
            }
        } else {
            if (rc.getMode() == RobotMode.PORTABLE) {
                tryMoveTo(optimalLocation);
                sharedArray.setMyArchonLocation(sharedArray.archonIdToIndex(rc.getID()), rc.getLocation());
                setSpawnDirections();
            } else {
                tryTransform();
            }
        }
    }

    private double getSpawnRubble(MapLocation location) throws GameActionException {
        double score = 0.0;

        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        Direction optimalDirection = location.directionTo(center);

        MapLocation optimalLocation = location.add(optimalDirection);
        if (rc.onTheMap(optimalLocation)) {
            score += rc.senseRubble(optimalLocation);
        }

        Direction left = optimalDirection;
        Direction right = optimalDirection;

        for (int i = 0; i < 3; i++) {
            left = left.rotateLeft();
            right = right.rotateRight();

            MapLocation locationLeft = location.add(left);
            if (rc.onTheMap(locationLeft)) {
                score += (double) (i + 2) * rc.senseRubble(locationLeft);
            }

            MapLocation locationRight = location.add(right);
            if (rc.onTheMap(locationRight)) {
                score += (double) (i + 2) * rc.senseRubble(locationRight);
            }
        }


        MapLocation oppositeLocation = location.add(optimalDirection.opposite());
        if (rc.onTheMap(oppositeLocation)) {
            score += 5.0 * rc.senseRubble(oppositeLocation);
        }

        return score;
    }
}
