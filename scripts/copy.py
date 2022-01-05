import shutil
import sys
from pathlib import Path

def fail(message):
    print(message)
    sys.exit(1)

def main():
    if len(sys.argv) != 3:
        fail("Usage: python scripts/copy.py <source name> <destination name>")

    source_name, destination_name = sys.argv[1:]

    source_directory = Path(__file__).parent.parent / "src" / source_name
    destination_directory = Path(__file__).parent.parent / "src" / destination_name

    if not source_directory.is_dir():
        fail(f"Bot '{source_name}' does not exist")

    if destination_directory.is_dir():
        fail(f"Bot '{destination_name}' already exists")

    print(f"Copying '{source_directory}' to '{destination_directory}'")

    shutil.copytree(source_directory, destination_directory)

    for file in destination_directory.rglob("*.java"):
        content = file.read_text(encoding="utf-8")
        content = content.replace(f"package {source_name}", f"package {destination_name}")
        content = content.replace(f"import {source_name}", f"import {destination_name}")
        file.write_text(content, encoding="utf-8")

if __name__ == "__main__":
    main()
