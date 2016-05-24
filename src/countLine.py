# imports
import os

# funcs

# gets all the files in the specified dir
def getFiles(d):
	files = []
	for f in os.listdir(d):
		fpf = os.path.join(d, f)
		if os.path.isfile(fpf):
			print("Found "+fpf+"...")
			files.append(fpf)
	return files
	
# gets all the files in the specified dir + all subdirs
def searchForFiles(d):
	files = []
	files.extend(getFiles(d))
	for f in os.listdir(d):
		sd = os.path.join(d, f)
		if not os.path.isfile(sd):
			files.extend(searchForFiles(sd))
	return files

# count the lines in f
def countLines(fname):
	with open(fname) as f:
		for i, l in enumerate(f):
			pass
	return i + 1

# main
cwd = os.getcwd()
print("Counting all lines of all files in: "+cwd)

print("Searching files...")
allFiles = searchForFiles(cwd)

print("\nTerminated, reading files...")

totalLineCount = 0
for f in allFiles:
	lc = countLines(f)
	print("Found "+str(lc)+" lines in: "+str(f))
	totalLineCount += lc

print("\nCounting finished: "+str(totalLineCount)+" lines found.")
