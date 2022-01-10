package camel_case_v01.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v01.dijkstra.Dijkstra34;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private RobotType[] spawnOrder1 = {
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    private RobotType[] spawnOrder2 = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
    };

    private RobotType[] spawnOrder = spawnOrder1;
    private int spawnOrderIndex = 0;

    public Archon(RobotController rc) {
        super(rc, RobotType.ARCHON, new Dijkstra34(rc));

        setSpawnDirections();
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        if (getAttackTarget(me.visionRadiusSquared) != null) {
            tryBuildRobot(RobotType.SOLDIER);
            return;
        }

        if (!isMyTurn()) {
            return;
        }

        if (rc.getRoundNum() >= 250 && spawnOrder == spawnOrder1) {
            spawnOrder = spawnOrder2;
            spawnOrderIndex = 0;
        }

        if (tryBuildRobot(spawnOrder[spawnOrderIndex])) {
            spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
        }
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

    private boolean isMyTurn() {
        int archonCount = rc.getArchonCount();
        if (archonCount == 1) {
            return true;
        }

        int myId = rc.getID();
        int archonIndex = myId % 2 == 0 ? myId / 2 - 1 : (myId - 1) / 2 - 1;

        int startOffset = 100 / archonCount * archonIndex;
        int endOffset = 100 / archonCount * (archonIndex + 1);

        int roundIndex = rc.getRoundNum() % 100;
        return roundIndex >= startOffset && roundIndex < endOffset;
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
}
