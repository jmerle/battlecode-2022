# Battlecode 2022

The Battlecode 2022 bots of team "camel_case".

Results:
- 9th-12th in the Sprint Tournament 1
- 33th-64th in the Sprint Tournament 2
- 3rd-4th in the International Qualifying Tournament
- 13th-16th in the Final Tournament

All bots can be found in the [`src`](./src) directory. The names of the bots indicate which bot was used in each tournament.

The [`scripts`](./scripts) directory contains some useful Python scripts:
- [`copy.py`](./scripts/copy.py) duplicates a bot and updates all `package` and `import` statements in the new bot accordingly.
- [`dijkstra.py`](./scripts/dijkstra.py) generates bytecode-efficient classes that perform a limited version of Dijkstra's algorithm, based on Mallot Fat Cats's 2021 pathfinding (see their [post-mortem](http://www.battlecode.org/files/postmortem-2021-malott-fat-cats.pdf) and [code](https://github.com/IvanGeffner/battlecode2021/blob/master/thirtyone/BFSPolitician.java)).
- [`run.py`](./scripts/run.py) runs two bots against each other on a list of maps, reporting the win rate of each bot, the maps which the two bots tie on (i.e. both bots win as either red or blue), and the maps which each bot dominates on (i.e. wins as both red and blue).
