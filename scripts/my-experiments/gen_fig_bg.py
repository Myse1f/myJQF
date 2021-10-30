import matplotlib.pyplot as plt
plt.switch_backend('Agg')
import matplotlib
plt.rcParams.update({
    'font.size': 18,
    'legend.fontsize': 18,
    'xtick.labelsize': 18,
    'ytick.labelsize': 18,
    'axes.titlesize': 18,
    'axes.labelsize' :18,
    'font.family': 'sans-serif'
})    
matplotlib.rcParams['pdf.fonttype'] = 42
matplotlib.rcParams['ps.fonttype'] = 42
import numpy as np
from load_data import DataLoader
import sys
import os
import os.path

if len(sys.argv) != 2:
    print("Usage: {} results-dir".format(sys.argv[0]))
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

gb_dl = DataLoader(basedir)
gb_dl.load_data()

### MAIN PLOTTING ###
def error_bars(runs, ytype, dl):
    ax = 0
    if ytype == "percent_valid":
        runs = [np.array(r[5])/np.array(r[4]) for r in runs]
    elif ytype == "percent_unique":
        runs = [np.array(r[8])/np.array(r[4]) for r in runs]
    elif ytype == "percent_upaths":
        runs = [np.array(r[6])/np.array(r[4]) for r in runs]
    else:
        dtype_idx = dl.sm[ytype]
        runs = [r[dtype_idx] for r in runs]
    
    mean = np.mean(runs, axis=ax)
    stderr = np.std(runs, axis=ax)/np.sqrt(len(runs))
    return mean, stderr

    
def plot_greybox(valid_bench, ytype):
    nicenames = {"black": "Blackbox Automata", "grey": "Greybox Automata"}
    fig, ax = plt.subplots(figsize=((6.2,4.2)))
    lss = iter(['-', '--'])
    colors = iter(["C2", "#91A3B0"])
    for tech in ['black', 'grey']:
        ls = next(lss)
        color = next(colors)
        base_dirname = f"{valid_bench}-{tech}-results"
        runs = gb_dl.get_data(base_dirname, 1)
        x = np.linspace(0, 5, 100)
        y, stderr = error_bars(runs, ytype, gb_dl)
        plt.plot(x, y, label=nicenames[tech], linestyle=ls, color=color, linewidth=2.5)
        plt.fill_between(x, y + stderr, y - stderr, alpha=0.3, color=color)
    
    plt.xlabel("Time (min)")
    if "percent" in ytype:
        ax = plt.gca()
        from matplotlib.ticker import PercentFormatter
        ax.yaxis.set_major_formatter(PercentFormatter(xmax=1))
    else:
        ax = plt.gca()
        from matplotlib.ticker import FuncFormatter
        ax.yaxis.set_major_formatter(FuncFormatter(lambda x, pos: "%i" %x if x < 1000 else "%ik" % (x/1000)))
        
    if ytype == "valid_paths":
        plt.ylabel("Diverse Valids")
    elif ytype == 'percent_upaths':
        plt.ylabel('% Diverse Valid')
    elif ytype == "valid_cov":
        plt.ylabel('Branch Cov. by Valids')
    else:
        plt.ylabel(ytype.split('_'))

    eg = plt.legend(loc='best')
    fignum = "Fig"
    plt.savefig(os.path.join(basedir,"figs/{}_{}.pdf".format(fignum, valid_bench)))

ytype = 'total_cov'
print("Generating Figure")
for bench in ['closure', 'rhino', 'fastjson', 'gson']:
    plot_greybox(bench, ytype)

