package camel_case.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private boolean hasSpawned = false;

    public Archon(RobotController rc) {
        super(rc, RobotType.ARCHON);

        setSpawnDirections();
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        if (!hasSpawned && tryBuildRobot(RobotType.SOLDIER)) {
            hasSpawned = true;
        }
    }

    private void setSpawnDirections() {
        spawnDirections = new Direction[8];

        MapLocation location = rc.getLocation();
        String dx = location.x <= rc.getMapWidth() / 2 ? "EAST" : "WEST";
        String dy = location.y <= rc.getMapHeight() / 2 ? "NORTH" : "SOUTH";

        spawnDirections[0] = Direction.valueOf(dy + dx);

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

        MapLocation myLocation = rc.getLocation();

        for (Direction direction : spawnDirections) {
            if (rc.canBuildRobot(type, direction)) {
                int rubble = rc.senseRubble(myLocation.add(direction));
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
