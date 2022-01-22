package camel_case_v18.util;

import battlecode.common.GameActionException;

public interface BattlecodeFunction<T, R> {
    R apply(T parameter) throws GameActionException;
}
