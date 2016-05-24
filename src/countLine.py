# imports
import os

# vars
logfilename = 'countLine0.log'

namestried = 1
while(os.path.exists(logfilename)):
	logfilename = 'countLine' + str(namestried) + '.log'

logfile = open(logfilename, 'w+')

# funcs
def log(what):
	logfile.write(what + "\n")
	print(what)
	
# gets all the files in the specified dir
def getFiles(d):
	files = []
	for f in os.listdir(d):
		fpf = os.path.join(d, f)
		if os.path.isfile(fpf):
			log("Found "+fpf+"...")
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
	i = 0
	
	with open(fname) as f:
		for i, l in enumerate(f):
			pass
	return i + 1

# main
cwd = os.getcwd()
log("Counting lines of all files in: "+cwd)

log("Searching for files...")
allFiles = searchForFiles(cwd)

log("\nTerminated, reading files...")

totalLineCount = 0
for f in allFiles:
	lc = countLines(f)
	log("Found "+str(lc)+" lines in: "+str(f))
	totalLineCount += lc

log("\nCounting finished: "+str(totalLineCount)+" lines found.")

logfile.close()
