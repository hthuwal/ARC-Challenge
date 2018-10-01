import sys
import os

file = os.path.abspath(sys.argv[1])
name, ext = os.path.splitext(file)
target_file = name + "_dair" + ext
ends = ['?', '.', '!']

with open(file, "r") as f, open(target_file, "w") as out:
    count = 0
    for line in f:
        line = line.strip()
        print("\r" + str(count), end=" ")
        if len(line.strip().split()) > 1:
            if line[-1] not in ends:
                line = line + "."
                out.write(line + "\n")
        count += 1
    print("\nDone\n")
