import json
import re
from collections import defaultdict

# gnf文法数据 
gnf_grammar = None
# 全局状态计数
state_count = 1
# 状态机状态转移规则
pda = []
# 工作集，元素为当前状态，以及栈中内容 
worklist = []
# 状态对应的栈信息
state_stacks = {} 
# 栈深度限制，当栈深度大于limit时，忽略此扩展
stack_limit = None 
# 由于深度限制，未扩展规则集
unexpanded_rules = set()

DEBUG = False

def main(grammar, out, limit, debug):
    global worklist, gnf_grammar, stack_limit, DEBUG
    DEBUG = debug
    current = '0'
    stack_limit = limit
    if stack_limit:
        print ('栈深度限制:' + str(stack_limit))

    with open(grammar, 'r') as fd:
        gnf_grammar = json.load(fd)
    start_symbol = gnf_grammar["Start"][0]
    worklist.append([current, [start_symbol]])
    filename = (grammar.split('/')[-1]).split('.')[0]

    while worklist:
        # 基于工作集
        if debug:
            print ('================')
            print ('Worklist:', worklist)
        element = worklist.pop(0)
        prep_transitions(element)
    transformed = postprocess()
    
    dump_file(out, transformed)

    if debug:
        dump_file(filename + '_transition.json', pda)

    if debug and unexpanded_rules:
        print ('由于栈深度限制，未扩展规则已存在{}_disallowed.json'.format(filename))
        dump_file(filename + '_disallowed.json', list(unexpanded_rules))


def prep_transitions(element):
    '''
    根据文法生成转换规则
    A -> aS, S is a sequnce of nonterminal
    pda : delta(p, a, e) = (q, S)
    fsa : delta(p, a) = (q), q is elements sequence in stack
    '''
    global gnf_grammar, state_count, pda, worklist, state_stacks, stack_limit, unexpanded_rules, DEBUG
    state = element[0]
    try:
        nonterminal = element[1][0] 
    except IndexError:
        # 栈为空，已到达final state
        return
    rules = gnf_grammar[nonterminal]
    count = 1
    for rule in rules:
        isRecursive  = False
        # print ('Current state:', state)
        terminal, nonterminals = tokenize(rule)
        transition = get_template()
        transition['trigger'] = '_'.join([state, str(count)])
        transition['source'] = state
        transition['dest'] = str(state_count) 
        transition['nonterminals'] = nonterminals 
        transition['terminal'] = terminal
        transition['rule'] = "{} -> {}".format(nonterminal, rule )
        
        # 创建对应状态的栈信息
        try:
            state_stack = state_stacks[state][:]
        except:
            state_stack = []
        if len(state_stack):
            state_stack.pop(0)
        if nonterminals:
            for symbol in nonterminals[::-1]:
                state_stack.insert(0, symbol)
        transition['stack'] = state_stack 

        # 检查栈是否有递归情况，有则增加一个跳转到已有状态的transition
        # print (state_stacks)
        if state_stacks:
            for state_element, stack in state_stacks.items():
                # print ('Stack:', stack)
                # print ('State stack:', state_stack)
                if stack == state_stack:
                    transition['dest'] = state_element
                    if DEBUG:
                        print ('Recursive:', transition)
                    pda.append(transition)
                    count += 1
                    isRecursive = True
                    break 

        if isRecursive:
            continue
            
        # 如果栈深度过大，则忽略此条规则 
        if stack_limit:
            if (len(transition['stack']) > stack_limit):
                unexpanded_rules.add(transition['rule'])
                continue

        pda.append(transition)
        worklist.append([transition['dest'], transition['stack']])
        state_stacks[transition['dest']] = state_stack
        state_count += 1
        count += 1

def tokenize(rule):
    '''
    获得GNF规则中的终结符和非终结符序列
    '''
    pattern = re.compile("\'([\s\S]+)\'([\s\S]*)")
    terminal = None
    nontermials = None
    match = pattern.match(rule)
    if match and match.group(1):
        terminal = match.group(1)
    else:
        raise AssertionError("Rule is not in GNF form")

    if match.group(2):
        nontermials = (match.group(2)).split()

    return terminal, nontermials

def get_template():
    transition_template = {
            'trigger':None,
            'source': None,
            'dest': None,
            'terminal' : None,
            'stack': []
            }
    return transition_template

def postprocess():
    '''
    创建json格式的自动机
    '''
    global pda
    final_struct = {}
    memoized = defaultdict(list)
    
    culled_pda = []
    culled_final = []
    num_transitions = 0 


    states, final, initial = _get_states()

    print ("initial", initial)
    print ("final", final)
    assert len(initial) == 1, 'More than one init state found'

    # 过滤由于栈深度限制的规则
    if stack_limit:

        blocklist = []
        for transition in pda:
            if (transition["dest"] in final) and (len(transition["stack"]) > 0):
                blocklist.append(transition["dest"])
                continue
            else:
                culled_pda.append(transition)
        
        culled_final = [state for state in final if state not in blocklist]
        print('culled_final', culled_final)
        assert len(culled_final) == 1, 'More than one final state found'

        for transition in culled_pda:
            state = transition["source"]
            if transition["dest"] in blocklist:
                    continue
            num_transitions += 1
            memoized[state].append([transition["trigger"], transition["dest"], 
                transition["terminal"]])
        final_struct["init_state"] = initial[0]
        final_struct["final_state"] = culled_final[0]
        
        print ('生成的状态数:', len(memoized.keys()))
        print ('状态转移数量:', num_transitions)
        print ('未过滤前状态数:', len(states))
        final_struct["numstates"] = len(states) 
        final_struct["pda"] = memoized
        return final_struct
    
    for transition in pda:
       state = transition["source"]
       memoized[state].append([transition["trigger"], transition["dest"], 
           transition["terminal"]])

    final_struct["init_state"] = initial
    final_struct["final_state"] = final[0]
    print ('状态数:', len(memoized.keys()))
    final_struct["numstates"] = len(memoized.keys()) 
    final_struct["pda"] = memoized
    return final_struct


def _get_states():
    '''
    获取状态信息
    [所有状态， 终止状态， 初始状态]
    '''
    source = set()
    dest = set()
    global pda
    for transition in pda:
        source.add(transition["source"])
        dest.add(transition["dest"])
    source_copy = source.copy()
    source_copy.update(dest)
    return list(source_copy), list(dest.difference(source)), list(source.difference(dest))

def dump_file(out, data):
    '''将json数据输出到文件'''
    with open(out, 'w+') as fd:
        json.dump(data, fd)

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description = 'Convert GNF to FSA(limited PDA)')
    parser.add_argument(
            '--grammar',
            type = str,
            help = 'Location of GNF grammar file')
    parser.add_argument(
            '--out',
            type = str,
            required = True,
            help = 'Location of output file')
    parser.add_argument(
            '--limit',
            type = int,
            default = None,
            help = 'Specify the upper bound for the stack size')
    parser.add_argument(
        '--debug',
        type=bool,
        default=False,
        help = 'Debug mode'
    )
    args = parser.parse_args()
    main(args.grammar, args.out, args.limit,  args.debug)