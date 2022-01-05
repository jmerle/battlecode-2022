package template;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import template.robot.Robot;
import template.robot.building.Archon;
import template.robot.building.Laboratory;
import template.robot.building.Watchtower;
import template.robot.droid.Builder;
import template.robot.droid.Miner;
import template.robot.droid.Sage;
import template.robot.droid.Soldier;

@SuppressWarnings("unused")
public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot robot = createRobot(rc);

        if (robot == null) {
            return;
        }

        // noinspection InfiniteLoopStatement
        while (true) {
            performTurn(rc, robot);
            Clock.yield();
        }
    }

    private static void performTurn(RobotController rc, Robot robot) {
        int startRound = rc.getRoundNum();

        try {
            robot.run();
        } catch (Exception e) {
            System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
            e.printStackTrace();
        }

        int usedBytecodes = (rc.getRoundNum() - startRound) * rc.getType().bytecodeLimit + Clock.getBytecodeNum();
        int maxBytecodes = rc.getType().bytecodeLimit;
        double bytecodePercentage = (double) usedBytecodes / (double) maxBytecodes * 100.0;
        if (bytecodePercentage >= 90) {
            String format = "High bytecode usage!\n%s/%s (%s%%)\n";
            System.out.printf(format, usedBytecodes, maxBytecodes, (int) Math.round(bytecodePercentage));
        }
    }

    private static Robot createRobot(RobotController rc) {
        switch (rc.getType()) {
            case ARCHON:
                return new Archon(rc);
            case LABORATORY:
                return new Laboratory(rc);
            case WATCHTOWER:
                return new Watchtower(rc);
            case BUILDER:
                return new Builder(rc);
            case MINER:
                return new Miner(rc);
            case SAGE:
                return new Sage(rc);
            case SOLDIER:
                return new Soldier(rc);
            default:
                System.out.println("Unknown robot type '" + rc.getType() + "'");
                return null;
        }
    }
}
