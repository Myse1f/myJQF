import matplotlib.pyplot as plt
plt.switch_backend('Agg')
import matplotlib

plt.rcParams.update({
    'font.size': 18,
    'legend.fontsize': 18,
    'font.family' : 'sans-serif',
    'xtick.labelsize': 18,
    'ytick.labelsize': 18
    
})    
matplotlib.rcParams['pdf.fonttype'] = 42
matplotlib.rcParams['ps.fonttype'] = 42
import numpy as np
from load_data import DataLoader

import sys
import os
import os.path

### MAIN PLOTTING ###
def get_x(run):
    x = run[dl.sm['unix_time']]
    x = (x - min(x)) / 60. # absolue time values in minutes
    return x

def error_bars(runs, ytype):
    ax = 0
    if ytype == "percent_valid":
        runs = [np.array(r[5])/np.array(r[4]) for r in runs]
    elif ytype == "percent_unique":
        runs = [np.array(r[8])/np.array(r[4]) for r in runs]
    elif ytype == "percent_upaths":
        runs = [np.array(r[6])/np.array(r[4]) for r in runs]
    else:
        if ytype == 'unique_valid':
            dtype_idx = 8
        else:
            dtype_idx = dl.sm[ytype]
        runs = [r[dtype_idx] for r in runs]

    mean = np.mean(runs, axis=ax)
    stderr = np.std(runs, axis=ax)/np.sqrt(len(runs))
    return mean, stderr

def plot(valid_bench, ytype, time):
    nicenames = {"rnd": "QuickCheck", "zest": "Zest", "automata": "Automata"}
    fig, ax = plt.subplots(figsize=((6.2,4.2)))
    lss = iter(reversed(['-', '--', '-.']))
    i = 0
    for tech in ['rnd', 'zest', 'automata']:
        color = 'C%i' % (i)
        i += 1
        ls = next(lss)
        base_dirname = f"{valid_bench}-{tech}-results"
        runs = dl.get_data(base_dirname, time)
        x = np.linspace(0, 5, 100)
        y, stderr = error_bars(runs, ytype)
        plt.plot(x, y, label=nicenames[tech], linestyle=ls, linewidth=2.5, color=color)
        plt.fill_between(x, y + stderr, y - stderr, alpha=0.3, color=color)
    
    plt.xlabel("Time (min)")
    if "percent" in ytype:
        ax = plt.gca()
        from matplotlib.ticker import PercentFormatter
        ax.yaxis.set_major_formatter(PercentFormatter(xmax=1))
    else:
        ax = plt.gca()
        from matplotlib.ticker import FuncFormatter
        ax.yaxis.set_major_formatter(FuncFormatter(lambda x, pos: "%i" %x if x < 1000 else "%ik" % (x//1000)))
        
    if ytype == "valid_paths":
        plt.ylabel("Diverse Valids")
        ax.yaxis.set_major_formatter(FuncFormatter(lambda x, pos: "%i" %x if x < 1000 else "%ik" % (x//1000)))
        
    elif ytype == 'percent_upaths':
        plt.ylabel('% Diverse Valid')
    elif ytype == "valid_cov":
        plt.ylabel('Branch Cov. by Valids')
    else:
        plt.ylabel(ytype)
    if valid_bench == "rhino" and ytype == "percent_upaths":
        leg = plt.legend(loc=(0.46,0.47))
    else:
        leg = plt.legend(loc='best')
    for line in leg.get_lines():
        line.set_linewidth(1.5)
        pass
    plt.tight_layout()
    figname = "Fig"
    plt.savefig(os.path.join(basedir, "figs/{}_{}.pdf".format(figname, valid_bench)))

if __name__=='__main__':
    if len(sys.argv) != 3:
        print("Usage: {} results-dir reps".format(sys.argv[0]))
        sys.exit()
    basedir = sys.argv[1]
    if not os.path.isdir(basedir):
        print("Usage: {} results-dir".format(sys.argv[0]))
        print("ERROR: {} is not a directory".format(basedir))
        sys.exit()
    else:
        try:
            os.mkdir(os.path.join(basedir, "figs"))
        except FileExistsError:
            # That's ok, we just wanted to create it in case it didn't exist.
            pass

    dl = DataLoader(basedir)
    dl.load_data()
    YTYPE = 'valid_cov'
    print("Generating Figure")
    for v in dl.validity:
        plot(v, YTYPE, int(sys.argv[2]))

