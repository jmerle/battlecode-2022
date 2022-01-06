package camel_case_v3.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v3.dijkstra.Dijkstra34;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private RobotType[] spawnOrder = {
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER
    };

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
