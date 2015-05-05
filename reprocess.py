#!/usr/bin/env python

import sys
import glob


def main(args):
    writeTxt = bool(int(args[1]))
    with open('grades_revised.csv', 'w') as f:
        f.write('NetID,Grade,Add Comments,\n')
        for file in glob.glob("*.txt"):
            f.write(reprocess(file, writeTxt))

def reprocess(fname, writeTxt):
    with open(fname) as f:
        line = [l for l in f]
    if '-' in line[-3] and '+' in line[-2]:
        correct = line[-5].strip()
        score = line[-4].strip()
        bonus = 3.
        penalty = -3.
        line = line [:-5]
    elif '-' in line[-2]:
        correct = line[-4].strip()
        score = line[-3].strip()
        penalty = -3.
        bonus = 0.
        line = line [:-4]
    elif '+' in line[-2]:
        correct = line[-4].strip()
        score = line[-3].strip()
        bonus = 3.
        penalty = 0.
        line = line[:-4]
    else:
        correct = line[-3].strip()
        score = line[-2].strip()
        bonus = 0.
        penalty = 0.
        line = line[:-3]
    cCol = correct.find(":")
    cFlo = float(correct[cCol+1:].strip())
    sCol = score.find(":")
    sFlo = float(score[sCol+1:].strip())
    if writeTxt:
        with open(fname[:-4] + '_revised.txt', 'w') as f:
            line.append('Correctness (80%%): %f%%\n' % cFlo)
            line.append('Points (20%%): %f%%\n' % sFlo)
            if penalty < 0:
                line.append(' - 3 point printing penalty\n')
            if bonus > 0:
                line.append(' + 3 point super solution bonus\n')
            line.append('Total: %f%%\n' % (cFlo*0.8 + sFlo*0.2 + penalty + bonus))
            f.writelines(line)
    return nameToGradesLine(fname, (cFlo*0.8 + sFlo*0.2 + penalty + bonus), line)

def nameToGradesLine(fname, grade, feedback):
    s = '"'
    for line in feedback:
        s += line
    s += '"'
    fname = fname[:-13]
    if '_' not in fname:
        return '%s,%f,%s,\n' % (fname, grade,s)
    lastU = fname.rfind('_')
    netId1 = fname[lastU+1:]
    fname = fname[:lastU]
    lastU = fname.rfind('_')
    netId2 = fname[lastU+1:]
    return '%s,%f,%s,\n%s,%f,%s,\n' % (netId1, grade, s, netId2, grade, s)


if __name__ == '__main__':
    main(sys.argv)
