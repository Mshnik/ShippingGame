#!/usr/bin/env python

import sys
import glob


def main(args):
    for file in glob.glob("*feedback.txt"):
        reprocess(file)

def reprocess(fname):
    with open(fname) as f:
        line = [l for l in f]
        orig = line[:]
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
    grade = max(0,(cFlo*0.8 + sFlo*0.2 + penalty + bonus))
    if grade > 100:
        with open("perfects/"+fname, 'w') as f:
            f.writelines(orig)


if __name__ == '__main__':
    main(sys.argv)
