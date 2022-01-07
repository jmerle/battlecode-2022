package camel_case_v9.robot.building;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import camel_case_v9.dijkstra.Dijkstra34;
import camel_case_v9.util.RandomUtils;

public class Archon extends Building {
    private Direction[] spawnDirections;

    private boolean isFirstRun = true;

    private int leadingMiners = 10;

    private RobotType[] spawnOrder = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.BUILDER,
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

            isFirstRun = false;
        }

        double archonCount = rc.getArchonCount();
        double turnIndex = sharedArray.getArchonTurnIndex();
        if (!RandomUtils.chance((turnIndex + 1) / archonCount)) {
            return;
        }

        if (getAttackTarget(me.visionRadiusSquared) != null) {
            tryBuildRobot(RobotType.SOLDIER);
            return;
        }

        if (leadingMiners > 0) {
            if (tryBuildRobot(RobotType.MINER)) {
                leadingMiners--;
            }

            return;
        }

        if (rc.getTeamLeadAmount(myTeam) < 500) {
            while (spawnOrder[spawnOrderIndex] == RobotType.BUILDER) {
                spawnOrderIndex = (spawnOrderIndex + 1) % spawnOrder.length;
            }
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
}
