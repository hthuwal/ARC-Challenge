import sys
import os
from tqdm import tqdm
from file_read_backwards import FileReadBackwards

def preprocess(line):
	line = line.strip()
	if (len(line) != 0 and line[-1] != '.'):
		line = line + "."
	return line

if __name__ == '__main__':
	target_dir = sys.argv[1]

	incomplete_files = []
	for i in tqdm(range(10000), ascii=True):
		i = format(i, '04d')
		file = os.path.join(target_dir, f"part_{i}")
		openie = file + ".openie"
		exceptions = file + ".exceptions"
		# print(f"Checking file: {file}...")
		
		for each in FileReadBackwards(file):
			each = each.strip()
			if each:
				last_line = preprocess(each)
				break
		# print(f"last_line: {last_line}")

		if os.path.exists(openie):
			last_parsed_line = None
			for line in FileReadBackwards(openie):
				if line.startswith("##"):
					last_parsed_line = line[2:].strip()
					# print(f"last_parsed_line: {last_parsed_line}")
					break

			if last_parsed_line and last_parsed_line == last_line:
				# print(file, "is complete.")
				continue

		if os.path.exists(exceptions):
			last_error_line = None
			for each in FileReadBackwards(file):
				each = each.strip()
				if each:
					last_error_line = each
					break

			if last_error_line == last_line:
				# print(file, "is complete")
				continue


		if os.path.exists(openie) or os.path.exists(exceptions):
			incomplete_files.append(file)
			# print(file, "is not complete")
	
	print("incomplete_files..")
	for file in incomplete_files:
		print(file)