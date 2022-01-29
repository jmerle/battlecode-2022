package camel_case_v26.robot;

import battlecode.common.AnomalyType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case_v26.dijkstra.Dijkstra;
import camel_case_v26.util.ArrayUtils;
import camel_case_v26.util.RandomUtils;
import camel_case_v26.util.SharedArray;

public abstract class Robot {
    protected RobotController rc;
    protected RobotType me;

    protected Team myTeam;
    protected Team enemyTeam;

    protected int mapWidth;
    protected int mapHeight;

    protected SharedArray sharedArray;

    private Dijkstra dijkstra;

    protected Direction[] adjacentDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST
    };

    private int[] attackPriorities = {
            2, // Archon
            6, // Laboratory
            4, // Watchtower
            1, // Miner
            3, // Builder
            7, // Soldier
            5 // Sage
    };

    private MapLocation previousMoveToTarget;
    private Direction previousMoveToDirection;

    private Direction[] wanderQuadrants;
    private int wanderQuadrantIndex;
    private MapLocation currentWanderTarget;

    public Robot(RobotController rc, RobotType type, Dijkstra dijkstra) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        sharedArray = new SharedArray(rc);

        this.dijkstra = dijkstra;
    }

    public void run() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, enemyTeam)) {
            if (robot.type == RobotType.ARCHON) {
                sharedArray.setEnemyArchonLocation(sharedArray.archonIdToIndex(robot.ID), robot.location);
            }
        }
    }

    protected boolean tryAttack(RobotInfo robot) throws GameActionException {
        if (rc.canAttack(robot.location)) {
            if (!me.isBuilding()) {
                if (robot.type.isBuilding()) {
                    tryMoveTo(robot.location);
                } else {
                    int currentRubble = rc.senseRubble(rc.getLocation());

                    for (Direction direction : adjacentDirections) {
                        if (!rc.canMove(direction)) {
                            continue;
                        }

                        MapLocation newLocation = rc.adjacentLocation(direction);
                        if (newLocation.distanceSquaredTo(robot.location) > me.actionRadiusSquared) {
                            continue;
                        }

                        if (rc.senseRubble(newLocation) >= currentRubble) {
                            continue;
                        }

                        if (tryMove(direction)) {
                            break;
                        }
                    }
                }
            }

            boolean didAttack = false;

            if (rc.canEnvision(AnomalyType.CHARGE)) {
                int chargeDamage = 0;

                for (RobotInfo enemyRobot : rc.senseNearbyRobots(me.actionRadiusSquared, enemyTeam)) {
                    if (!enemyRobot.type.isBuilding()) {
                        chargeDamage += Math.min(enemyRobot.health, Math.floor((double) enemyRobot.type.getMaxHealth(enemyRobot.level) / 100.0 * 22.0));
                    }
                }

                if (chargeDamage > me.damage) {
                    rc.envision(AnomalyType.CHARGE);
                    didAttack = true;
                }
            }

            if (!didAttack && rc.canAttack(robot.location)) {
                rc.attack(robot.location);
                didAttack = true;
            }

            if (didAttack && robot.type == RobotType.ARCHON) {
                RobotInfo newRobot = rc.senseRobotAtLocation(robot.location);
                if (newRobot == null || newRobot.team == myTeam || newRobot.type != RobotType.ARCHON) {
                    sharedArray.setEnemyArchonLocation(sharedArray.archonIdToIndex(robot.ID), null);
                }
            }

            if (robot.type.canAttack() && rc.isMovementReady() && rc.getHealth() != me.getMaxHealth(rc.getLevel())) {
                tryMoveToSafety();
            }

            return true;
        }

        return false;
    }

    protected RobotInfo getAttackTarget(int radius) {
        RobotInfo bestTarget = null;
        int minHealth = Integer.MAX_VALUE;
        int maxPriority = Integer.MIN_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(radius, enemyTeam)) {
            int priority = attackPriorities[robot.type.ordinal()];
            if (bestTarget == null
                    || priority > maxPriority
                    || (robot.health < minHealth && priority == maxPriority)) {
                bestTarget = robot;
                minHealth = robot.health;
                maxPriority = priority;
            }
        }

        return bestTarget;
    }

    protected MapLocation getArchonTarget() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation bestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 5; i++) {
            MapLocation archon = sharedArray.getEnemyArchonLocation(i);
            if (archon == null) {
                continue;
            }

            if (myLocation.distanceSquaredTo(archon) <= me.actionRadiusSquared) {
                sharedArray.setEnemyArchonLocation(i, null);
                continue;
            }

            int distance = myLocation.distanceSquaredTo(archon);
            if (distance < minDistance) {
                bestTarget = archon;
                minDistance = distance;
            }
        }

        return bestTarget;
    }

    protected MapLocation getPossibleArchonTarget() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation bestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 15; i++) {
            MapLocation possibleEnemyArchon = sharedArray.getPossibleEnemyArchonLocation(i);
            if (possibleEnemyArchon == null) {
                continue;
            }

            if (rc.canSenseLocation(possibleEnemyArchon)) {
                sharedArray.setPossibleEnemyArchonLocation(i, null);
                continue;
            }

            int distance = myLocation.distanceSquaredTo(possibleEnemyArchon);
            if (distance < minDistance) {
                bestTarget = possibleEnemyArchon;
                minDistance = distance;
            }
        }

        return bestTarget;
    }

    protected void lookForDangerTargets() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        int enemyCount = 0;

        RobotInfo closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (RobotInfo robot : nearbyRobots) {
            if (robot.team != enemyTeam) {
                continue;
            }

            enemyCount++;

            int distance = myLocation.distanceSquaredTo(robot.location);
            if (distance < minDistance) {
                closestTarget = robot;
                minDistance = distance;
            }
        }

        if (closestTarget == null) {
            return;
        }

        int defenderCount = me.canAttack() ? 1 : 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.team == myTeam
                    && robot.type.canAttack()
                    && robot.location.distanceSquaredTo(closestTarget.location) <= robot.type.actionRadiusSquared) {
                defenderCount++;
            }
        }

        if (enemyCount > defenderCount) {
            sharedArray.addDangerTarget(closestTarget.location, 1);

            if (closestTarget.type == RobotType.SAGE) {
                MapLocation runAwayLocation = closestTarget.location;
                Direction runAwayDirection = myLocation.directionTo(closestTarget.location);

                for (int j = 0; j < 3; j++) {
                    MapLocation newRunAwayLocation = runAwayLocation.add(runAwayDirection);
                    if (newRunAwayLocation.x < 0
                            || newRunAwayLocation.x >= mapWidth
                            || newRunAwayLocation.y < 0
                            || newRunAwayLocation.y >= mapHeight) {
                        break;
                    }

                    runAwayLocation = newRunAwayLocation;
                }

                sharedArray.addDangerTarget(runAwayLocation, 10);
            }
        }
    }

    protected MapLocation getClosestDangerTarget() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 20; i++) {
            MapLocation target = sharedArray.getDangerTarget(i);
            if (target == null) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(target);
            if (distance < minDistance) {
                closestTarget = target;
                minDistance = distance;
            }
        }

        return closestTarget;
    }

    protected boolean tryMove(Direction direction) throws GameActionException {
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        return false;
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        Direction blockedDirection = target.equals(previousMoveToTarget) && previousMoveToDirection != null
                ? previousMoveToDirection.opposite()
                : null;

        previousMoveToTarget = target;

        Direction bestDirection = dijkstra.getBestDirection(target, blockedDirection);
        if (bestDirection != null && tryMove(bestDirection)) {
            previousMoveToDirection = bestDirection;
            return true;
        }

        return false;
    }

    protected boolean tryMoveRandom() throws GameActionException {
        for (Direction direction : ArrayUtils.shuffle(adjacentDirections.clone())) {
            if (tryMove(direction)) {
                return true;
            }
        }

        return false;
    }

    protected boolean tryWander() throws GameActionException {
        if (wanderQuadrants == null) {
            wanderQuadrants = ArrayUtils.shuffle(new Direction[]{
                    Direction.NORTHEAST,
                    Direction.NORTHWEST,
                    Direction.SOUTHEAST,
                    Direction.SOUTHWEST
            });

            wanderQuadrantIndex = -1;
        }

        if (currentWanderTarget == null || rc.canSenseLocation(currentWanderTarget)) {
            wanderQuadrantIndex = (wanderQuadrantIndex + 1) % 4;

            int minX = -1;
            int maxX = -1;
            int minY = -1;
            int maxY = -1;

            switch (wanderQuadrants[wanderQuadrantIndex]) {
                case NORTHEAST:
                    minX = mapWidth / 2;
                    maxX = mapWidth;
                    minY = mapHeight / 2;
                    maxY = mapHeight;
                    break;
                case NORTHWEST:
                    minX = 0;
                    maxX = mapWidth / 2;
                    minY = mapHeight / 2;
                    maxY = mapHeight;
                    break;
                case SOUTHEAST:
                    minX = mapWidth / 2;
                    maxX = mapWidth;
                    minY = 0;
                    maxY = mapHeight / 2;
                    break;
                case SOUTHWEST:
                    minX = 0;
                    maxX = mapWidth / 2;
                    minY = 0;
                    maxY = mapHeight / 2;
                    break;
            }

            currentWanderTarget = new MapLocation(RandomUtils.nextInt(minX, maxX), RandomUtils.nextInt(minY, maxY));
            return tryWander();
        }

        return tryMoveTo(currentWanderTarget);
    }

    protected boolean tryMoveToSafety() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(me.visionRadiusSquared, enemyTeam);

        Direction bestDirection = null;
        int minRubble = rc.senseRubble(myLocation);

        int maxDistance = 0;
        for (RobotInfo robot : enemyRobots) {
            if (robot.type.canAttack()) {
                maxDistance += myLocation.distanceSquaredTo(robot.location);
            }
        }

        for (Direction direction : adjacentDirections) {
            if (!rc.canMove(direction)) {
                continue;
            }

            MapLocation newLocation = rc.adjacentLocation(direction);
            int rubble = rc.senseRubble(newLocation);
            if (Math.abs(rubble - minRubble) > 20) {
                continue;
            }

            int distance = 0;
            for (RobotInfo robot : enemyRobots) {
                if (robot.type.canAttack()) {
                    distance += newLocation.distanceSquaredTo(robot.location);
                }
            }

            if ((distance > maxDistance && rubble <= minRubble) || (distance == maxDistance && rubble < minRubble)) {
                bestDirection = direction;
                maxDistance = distance;
                minRubble = rubble;
            }
        }

        if (bestDirection != null) {
            tryMove(bestDirection);
            return true;
        }

        return false;
    }

    protected boolean tryMoveToArchon() throws GameActionException {
        MapLocation closestArchon = getClosestArchon();

        if (closestArchon != null) {
            if (rc.getLocation().distanceSquaredTo(closestArchon) > 20) {
                tryMoveTo(closestArchon);
            } else {
                tryMoveRandom();
            }

            return true;
        }

        return false;
    }

    protected MapLocation getClosestArchon() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation closestArchon = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 5; i++) {
            MapLocation archon = sharedArray.getMyArchonLocation(i);
            if (archon == null) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(archon);
            if (distance < minDistance) {
                closestArchon = archon;
                minDistance = distance;
            }
        }

        return closestArchon;
    }

    protected boolean tryMoveToAndAttack(MapLocation location) throws GameActionException {
        if (location == null) {
            return false;
        }

        tryMoveTo(location);

        if (rc.canSenseLocation(location)) {
            RobotInfo robot = rc.senseRobotAtLocation(location);
            if (robot != null) {
                tryAttack(robot);
            }
        }

        return true;
    }
}
