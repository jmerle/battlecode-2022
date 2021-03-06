package camel_case_v01.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case_v01.dijkstra.Dijkstra;
import camel_case_v01.util.ArrayUtils;
import camel_case_v01.util.RandomUtils;

public abstract class Robot {
    protected RobotController rc;
    protected RobotType me;

    protected Team myTeam;
    protected Team enemyTeam;

    protected int mapWidth;
    protected int mapHeight;

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
            0, // Miner
            0, // Builder
            4, // Soldier
            5 // Sage
    };

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

        this.dijkstra = dijkstra;
    }

    public void run() throws GameActionException {
    }

    protected boolean tryAttack(MapLocation location) throws GameActionException {
        if (rc.canAttack(location)) {
            rc.attack(location);
            return true;
        }

        return false;
    }

    protected RobotInfo getAttackTarget(int radius) {
        RobotInfo bestTarget = null;
        int minDistance = Integer.MAX_VALUE;
        int maxPriority = Integer.MIN_VALUE;

        MapLocation myLocation = rc.getLocation();

        for (RobotInfo robot : rc.senseNearbyRobots(radius, enemyTeam)) {
            int priority = attackPriorities[robot.type.ordinal()];
            if (priority == 0) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(robot.location);

            if (bestTarget == null
                    || priority > maxPriority
                    || (distance < minDistance && priority == maxPriority)) {
                bestTarget = robot;
                minDistance = distance;
                maxPriority = priority;
            }
        }

        return bestTarget;
    }

    protected boolean tryMove(Direction direction) throws GameActionException {
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        return false;
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        Direction bestDirection = dijkstra.getBestDirection(target);
        return bestDirection != null && tryMove(bestDirection);
    }

    protected boolean tryMoveRandom() throws GameActionException {
        for (Direction direction : ArrayUtils.shuffle(adjacentDirections)) {
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
