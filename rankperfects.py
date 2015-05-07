#!/usr/bin/env python

import sys
import glob


def main(args):
    with open('Ranks.csv', 'w') as f:
        f.write('NetID,Score,\n')
        for file in glob.glob("*feedback.txt"):
            f.write(reprocess(file))

def reprocess(fname):
    with open(fname) as f:
        line = [l for l in f]
    st = line[-7]
    start= st.find(":")
    end = st.find("of")
    score = int(st[start+1:end])
    return nameToGradesLine(fname, score)

def nameToGradesLine(fname, score):
    fname = fname[:-13]
    if '_' not in fname:
        return '%s,%d,\n' % (fname, score)
    lastU = fname.rfind('_')
    netId1 = fname[lastU+1:]
    fname = fname[:lastU]
    lastU = fname.rfind('_')
    netId2 = fname[lastU+1:]
    return '%s and %s,%d\n' % (netId1, netId2, score)


if __name__ == '__main__':
    main(sys.argv)
