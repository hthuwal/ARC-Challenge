with open("6-7-8-9-10.txt", "r") as f, open("clean_ncert.txt", "w") as out:
    prev = ""
    for line in f:
        # print("$" + line + "$")
        # input()
        if line.strip() == "" and prev != "":
            out.write(prev + "\n\n")
            prev = ""
        else:
            prev += (line.strip() + " ")

    if(prev != ""):
        out.write(prev + "\n")
