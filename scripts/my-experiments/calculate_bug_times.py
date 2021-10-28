#!/usr/bin/python3

# This script should be run inside an aggregated results directory (e.g. pre-baked or fresh-baked)
# Typically, this script will not be launched by a human but by other scripts

from typing import Dict, Tuple, List, Set
from csv import reader, writer, DictReader
from collections import defaultdict
import sys


# Bugs are represented as tuple of (bench, exception)
Bug = Tuple[str, str]

# Crash times are a list of (elapsed_time, num_crashes)
CrashTimes = List[Tuple[int, int]]

# Bug data maps bugs to a map of techniques to a list of "time-to-crash"
# List size should be equal to number of repetitions in which bug was found
bug_data:Dict[Bug, Dict[str, List[int]]] = defaultdict(lambda: defaultdict(list))

def main(repetitions:int) -> None:
	for i in range(repetitions):
		for bench in ["closure", "rhino", "fastjson", "gson"]:
			for tech in ["zest", "rnd", "automata"]:
				try:
					process(bench, tech, i+1)
				except FileNotFoundError:
					pass # Ignore if results.csv is not found

	table_csv:str = 'figs/Table.csv'
	with open(table_csv, 'w') as csvfile:
		csv = writer(csvfile)
		csv.writerow(['benchmark', 'exception', 'tool', 'mtf', 'repeatibility'])
		for bug, data in bug_data.items():
			bench:str = bug[0]
			ex:str = bug[1]
			for technique, times in data.items():
				mtf:float = sum(times)/float(len(times))
				repeatibility:float = len(times)/repetitions
				csv.writerow([bench, ex, technique, mtf, repeatibility])


def process(bench:str, tech:str, id:int) -> None:
	# Remember the bugs processed in this run
	bugs_found:Set[Bug] = set()

	# Load crash times from plot_data
	results_dir:str = bench + '-' + tech + '-results-' + str(id)
	crash_times:CrashTimes = load_crash_times(results_dir)

	# Read results.csv in the fuzz results directory
	results_csv:str = results_dir + '/results.csv'
	with open(results_csv) as csvfile:
		csv = reader(csvfile, delimiter=',')
		for row in csv:
			# Only process entries which resulted in FAILURE
			input:str = row[0]
			result:str = row[-2]
			ex:str = row[-1]
			if result == 'FAILURE':
				# Extract 5-digit input ID
				crash_id:int
				if input.startswith("failures/id"):
					# Format will be 'failures/id_xxxxxx'
					crash_id = int(input[-6:-1])
				elif input.startswith("crashes/id"):
					# Format will be 'crashes/id:xxxxxx'
					crash_id = int(input[11:])
				else:
					continue # Other stuff
				bug:Bug = (bench, ex)
				if bug not in bugs_found:
					bugs_found.add(bug)
					bug_find_time:int = find_crash_time(crash_id, crash_times)
					bug_data[bug][tech].append(bug_find_time)

def load_crash_times(results_dir:str) -> CrashTimes:
	# Remember start time (will be set when first row is read)
	start_time:int = -1

	# Accumulate result
	crash_times:CrashTimes = list()

	# Read plot data CSV
	plot_data:str = results_dir + '/plot_data'
	with open(plot_data) as csvfile:
		csv = DictReader(csvfile, delimiter=',', skipinitialspace=True)
		for row in csv:
			time:int = int(row['# unix_time'])
			num_crashes:int = int(row['unique_crashes'])
			# Set start time if not already done so
			if start_time < 0:
				start_time = time
			# Calculate elapsed time since start
			elapsed_time:int = time - start_time
			crash_times.append([elapsed_time, num_crashes])

	return crash_times

def find_crash_time(crash_id:int, crash_times: CrashTimes) -> int:
	for t in crash_times:
		elapsed_time:int = t[0]
		num_crashes:int = t[1]
		if num_crashes > crash_id:
			return elapsed_time
	raise Exception("Could not find time for crash ID " + str(crash_id))

if __name__ == "__main__":
	if len(sys.argv) < 2:
		print("Usage: " + sys.argv[0] + " NUM_REPS")
		sys.exit(1)
	main(int(sys.argv[1]))
