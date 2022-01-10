package camel_case.util;

import battlecode.common.GameActionException;

public interface BattlecodeFunction<T, R> {
    R apply(T parameter) throws GameActionException;
}
