package camel_case_v16_sprint1_sprint2.util;

import battlecode.common.GameActionException;

public interface BattlecodeFunction<T, R> {
    R apply(T parameter) throws GameActionException;
}
