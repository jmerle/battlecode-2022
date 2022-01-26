package camel_case_v21_international_qualifier.util;

import battlecode.common.GameActionException;

public interface BattlecodeFunction<T, R> {
    R apply(T parameter) throws GameActionException;
}
