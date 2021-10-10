import json
import random

def main(automata_file, num):
    automata = None
    with open(automata_file, 'r') as fd:
        automata = json.load(fd)

    pda = automata['pda']
    init_state = automata['init_state']
    final_state = automata['final_state']

    state = init_state
    out = ''
    while state != final_state:
        trigger_len = len(pda[state])
        idx = random.randint(0, trigger_len-1)
        trigger = pda[state][idx]
        state = trigger[1]
        out += trigger[2]
    
    print (out)

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description = 'Script to generate input from automata')
    parser.add_argument(
            '--auto',
            type = str,
            required  = True,
            help = 'Location of automata file')
    parser.add_argument(
            '--count',
            type = int,
            default = 1,
            help = 'The number of generated input')
    args = parser.parse_args()
    main(args.auto, args.count)