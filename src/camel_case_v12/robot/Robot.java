package camel_case_v12.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case_v12.dijkstra.Dijkstra;
import camel_case_v12.util.ArrayUtils;
import camel_case_v12.util.RandomUtils;
import camel_case_v12.util.SharedArray;

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
            7, // Archon
            3, // Laboratory
            6, // Watchtower
            2, // Miner
            1, // Builder
            4, // Soldier
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

            if (rc.canAttack(robot.location)) {
                rc.attack(robot.location);
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

    protected void lookForDangerTargets() throws GameActionException {
        int defenderCount = me.canAttack() ? 1 : 0;
        int enemyCount = 0;

        MapLocation myLocation = rc.getLocation();

        MapLocation closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared)) {
            if (robot.team == myTeam) {
                if (robot.type.canAttack()) {
                    defenderCount++;
                }
            } else {
                enemyCount++;

                int distance = myLocation.distanceSquaredTo(robot.location);
                if (distance < minDistance) {
                    closestTarget = robot.location;
                    minDistance = distance;
                }
            }
        }

        if (enemyCount > 0 && enemyCount >= defenderCount) {
            for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
                if (sharedArray.getDangerTarget(i) == null) {
                    sharedArray.setDangerTarget(i, closestTarget);
                }
            }
        }
    }

    protected void removeInvalidDangerTargets() throws GameActionException {
        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation target = sharedArray.getDangerTarget(i);
            if (target != null && rc.canSenseLocation(target)) {
                RobotInfo robot = rc.senseRobotAtLocation(target);
                if (robot == null || robot.team == myTeam) {
                    sharedArray.setDangerTarget(i, null);
                }
            }
        }
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
}
