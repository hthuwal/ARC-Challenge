import sys
import os
from tqdm import tqdm

def preprocess(line):
	line = line.strip()
	if (len(line) != 0 and line[-1] != '.'):
		line = line + "."
	return line

if __name__ == '__main__':
	target_dir = sys.argv[1]

	incomplete_files = []
	for i in tqdm(range(10000)):
		i = format(i, '04d')
		file = os.path.join(target_dir, f"part_{i}")
		openie = file + ".openie"
		exceptions = file + ".exceptions"

		# print(f"Checking file: {file}...")
		
		lines = [each.strip() for each in open(file).readlines()]
		lines = [each for each in lines if each]
		last_line = preprocess(lines[-1].strip())
		# print(f"last_line: {last_line}")
		del lines

		if os.path.exists(openie):
			lines = open(openie).readlines()
			last_parsed_line = None

			for i in range(-1, -len(lines)-1, -1):
				line = lines[i]
				if line.startswith("##"):
					last_parsed_line = line[2:].strip()
					# print(f"last_parsed_line: {last_parsed_line}")
					break

			del lines

			if last_parsed_line and last_parsed_line == last_line:
				# print(file, "is complete.")
				continue

		if os.path.exists(exceptions):
			lines = [each.strip() for each in open(exceptions).readlines()]
			lines = [each for each in lines if each]
			last_error_line = lines[-1].strip()
			# print(f"last_error_line: {last_error_line}")
			del lines

			if last_error_line == last_line:
				# print(file, "is complete")
				continue


		if os.path.exists(openie) or os.path.exists(exceptions):
			incomplete_files.append(file)
			# print(file, "is not complete")
	
	for file in incomplete_files:
		print(file)