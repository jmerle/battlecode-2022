import re
import signal
import subprocess
import sys
from datetime import datetime
from multiprocessing import Pool, Value
from pathlib import Path

def run_matches(bot1, bot2, maps, timestamp):
    result = {
        "bot1": bot1,
        "bot2": bot2
    }

    winners_by_map = {}
    current_map = None

    args = [
        str(Path(__file__).parent.parent / "gradlew"),
        "run",
        f"-PteamA={bot1}",
        f"-PteamB={bot2}",
        f"-Pmaps={','.join(maps)}",
        f"-PreplayPath=replays/run-{timestamp}-%TEAM_A%-vs-%TEAM_B%.bc22"
    ]

    proc = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    lines = []
    while True:
        line = proc.stdout.readline()
        if not line:
            break

        line = line.decode("utf-8").rstrip()
        lines.append(line)

        map_match = re.search(r"[^ ]+ vs\. [^ ]+ on ([^ ]+)", line)
        if map_match is not None:
            current_map = map_match[1]

        result_match = re.search(r"([^ ]+) \([AB]\) wins \(round (\d+)\)", line)
        if result_match is not None:
            global counter
            with counter.get_lock():
                counter.value += 1
                current_match = counter.value

            total_matches = len(maps) * 2
            prefix = f"[{str(current_match).rjust(len(str(total_matches)))}/{total_matches}]"

            print(f"{prefix} {result_match[1]} wins in {result_match[2]} rounds in {bot1} (red) versus {bot2} (blue) on {current_map}")

            winners_by_map[current_map] = result_match[1]

    if proc.wait() != 0:
        result["type"] = "error"
        result["message"] = "\n".join(lines)
        return result

    result["type"] = "success"
    result["winners"] = winners_by_map
    return result

def main():
    signal.signal(signal.SIGINT, lambda a, b: sys.exit(1))

    if len(sys.argv) != 3:
        print("Usage: python scripts/run.py <bot 1 name> <bot 2 name>")
        sys.exit(1)

    bot1 = sys.argv[1]
    bot2 = sys.argv[2]

    # Based on SERVER_MAPS in https://github.com/battlecode/battlecode22/blob/main/client/visualizer/src/constants.ts
    maps = [
        # Default
        # "maptestsmall",
        "eckleburg",
        "intersection",

        # Sprint 1
        "colosseum",
        "fortress",
        "jellyfish",
        "nottestsmall",
        "progress",
        "rivers",
        "sandwich",
        "squer",
        "uncomfortable",
        "underground",
        "valley",

        # Sprint 2
        "chessboard",
        "collaboration",
        "dodgeball",
        "equals",
        "highway",
        "nyancat",
        "panda",
        "pillars",
        "snowflake",
        "spine",
        "stronghold",
        "tower",

        # International qualifier
        "charge",
        "definitely_not_league",
        "fire",
        "highway_redux",
        "lotus",
        "maze",
        "olympics",
        "one_river",
        "planets",
        "snowflake_redux",
        "treasure",
        "walls",

        # US qualifier
        "chalice",
        "cobra",
        "deer",
        "desert",
        "despair",
        "flowers",
        "island_hopping",
        "octopus_game",
        "rugged",
        "snowman",
        "tunnels",
        "vault"
    ]

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    global counter
    counter = Value("i", 0)

    print(f"Running {len(maps) * 2} matches")

    with Pool(2) as pool:
        results = pool.starmap(run_matches, [(bot1, bot2, maps, timestamp),
                                             (bot2, bot1, maps, timestamp)])

    if any(r["type"] == "error" for r in results):
        for r in results:
            if r["type"] == "error":
                print(f"{r['bot1']} versus {r['bot2']} failed with the following error:")
                print(r["message"])
        sys.exit(1)

    map_winners = {}

    bot1_wins = 0
    bot2_wins = 0

    for r in results:
        for map, winner in r["winners"].items():
            if map in map_winners and map_winners[map] != winner:
                map_winners[map] = "Tied"
            else:
                map_winners[map] = winner

            if winner == bot1:
                bot1_wins += 1
            else:
                bot2_wins += 1

    tied_maps = [k for k, v in map_winners.items() if v == "Tied"]
    bot1_superior_maps = [k for k, v in map_winners.items() if v == bot1]
    bot2_superior_maps = [k for k, v in map_winners.items() if v == bot2]

    if len(tied_maps) > 0:
        print(f"Tied maps ({len(tied_maps)}):")
        for map in tied_maps:
            print(f"- {map}")
    else:
        print(f"There are no tied maps")

    if len(bot1_superior_maps) > 0:
        print(f"Maps {bot1} wins on as both red and blue ({len(bot1_superior_maps)}):")
        for map in bot1_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {bot1} wins on as both red and blue")

    if len(bot2_superior_maps) > 0:
        print(f"Maps {bot2} wins on as both red and blue ({len(bot2_superior_maps)}):")
        for map in bot2_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {bot2} wins on as both red and blue")

    print(f"{bot1} wins: {bot1_wins} ({bot1_wins / (bot1_wins + bot2_wins) * 100:,.2f}% win rate)")
    print(f"{bot2} wins: {bot2_wins} ({bot2_wins / (bot1_wins + bot2_wins) * 100:,.2f}% win rate)")

if __name__ == "__main__":
    main()
