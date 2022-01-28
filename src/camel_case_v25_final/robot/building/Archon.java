package camel_case_v25_final.robot.building;

import battlecode.common.AnomalyScheduleEntry;
import battlecode.common.AnomalyType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotMode;
import battlecode.common.RobotType;
import camel_case_v25_final.dijkstra.Dijkstra34;
import camel_case_v25_final.util.RandomUtils;
import camel_case_v25_final.util.SharedArray;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private boolean isFirstRun = true;

    private MapLocation optimalLocation = null;
    private boolean hasFoundInitialOptimalLocation = false;

    private AnomalyScheduleEntry[] anomalySchedule;

    private int minersSpawned = 0;
    private int maxLeadingMiners;

    private RobotType[] spawnOrder = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.BUILDER
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

        if (getAttackTarget(me.visionRadiusSquared) != null) {
            tryBuildRobot(RobotType.SOLDIER);
            tryRepair();
            return;
        }

        if (rc.getRoundNum() > 1 && !RandomUtils.chance(((double) turnIndex + 1) / (double) archonCount)) {
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

            tryRepair();
            return;
        }

        if (rc.getTeamLeadAmount(myTeam) < 300) {
            while (spawnOrder[spawnOrderIndex] == RobotType.BUILDER) {
                spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
            }
        }

        if (tryBuildRobot(spawnOrder[spawnOrderIndex])) {
            spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
        }

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

        if (bestDirection != null) {
            rc.buildRobot(type, bestDirection);
            return true;
        }

        return false;
    }

    private void tryRepair() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        RobotInfo repairTarget = getRepairTarget(me.actionRadiusSquared);
        if (repairTarget != null
                && repairTarget.health < repairTarget.type.getMaxHealth(repairTarget.level)
                && rc.canRepair(repairTarget.location)) {
            rc.repair(repairTarget.location);
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
