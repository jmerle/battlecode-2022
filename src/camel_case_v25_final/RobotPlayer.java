package camel_case_v25_final;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import camel_case_v25_final.robot.Robot;
import camel_case_v25_final.robot.building.Archon;
import camel_case_v25_final.robot.building.Laboratory;
import camel_case_v25_final.robot.building.Watchtower;
import camel_case_v25_final.robot.droid.Builder;
import camel_case_v25_final.robot.droid.Miner;
import camel_case_v25_final.robot.droid.Sage;
import camel_case_v25_final.robot.droid.Soldier;

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
        if (bytecodePercentage >= 95) {
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
