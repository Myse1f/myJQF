import json
import re
import copy
from string import ascii_uppercase
from itertools import combinations
from collections import defaultdict

NONTERMINALS = []
COUNT = 1

def main(grammar_file, out, start, debug):
    grammar = None
    # 读取文法文件
    if grammar_file.endswith(".json"):
        with open(grammar_file, 'r') as fd:
            grammar = json.load(fd)
    elif grammar_file.endswith(".g4"):
        with open(grammar_file, 'r') as fd:
            data = fd.readlines()
        grammar = g4tojson(data)
    else:
        raise('Only g4 or json file accept!')
    
    if debug:
        dump_file('debug_prepossess.json',grammar)
    # 1. CFG转换成CNF乔姆斯基范式
    # 1.1 Remove the null unit and useless production
    grammar = remove_unit(grammar)
    if debug:
        dump_file('debug_unit.json',grammar)
    # 1.2 Eliminate terminals from RHS of the production if they exist with other non-terminals or terminals
    grammar = eliminate_mixed(grammar)
    if debug:
        dump_file('debug_mixed.json',grammar)
    # 1.3 Eliminate the RHS with more than two non-terminals
    grammar = eliminate_multi(grammar)
    if debug:
        dump_file('debug_multi.json',grammar)
    # 2. CNF转换成GNF
    grammar = cnf2gnf(grammar)
    if debug:
        dump_file('debug_gnf.json',grammar)

    grammar['Start'] = [start]
    dump_file(out, grammar)

def dump_file(out, data):
    '''将json数据输出到文件'''
    with open(out, 'w+') as fd:
        json.dump(data, fd)


def g4tojson(data):
    '''将类g4格式的CFG转换成json格式，方便解析'''
    productions = []
    production = []
    for line in data:
        if line != '\n': 
            production.append(line)
        else:
            productions.append(production)
            production = []

    final_rule_set = {}
    for production in productions:
        rules = []
        init = production[0]
        nonterminal = init.split(':', 1)[0]
        rules.append(strip_chars(init.split(':', 1)[1]).strip('| '))
        for production_rule in production[1:]:
            rules.append(strip_chars(production_rule.split('|')[0]))
        final_rule_set[nonterminal] = rules

    return final_rule_set

def remove_unit(grammar):
    '''
    消除文法中的单元推导规则 
    eg. 
    S -> A 
    A -> 'a' 
    ==> 
    S -> 'a' 
    '''
    nonunit = False
    old_grammar = copy.deepcopy(grammar)
    new_grammar = defaultdict(list)
    # 循环消除直到没有单元推导规则
    while not nonunit:
        for lhs, rules in old_grammar.items():
            for rule in rules:
                # 检查是否是单元推导规则
                if len(get_tokens(rule)) == 1:
                    if not is_terminal(rule):
                        new_grammar[lhs].extend([r for r in old_grammar[rule]])
                    else:
                        new_grammar[lhs].append(rule)
                else:
                    new_grammar[lhs].append(rule)

        # 检查新语法中是否还有单元推导规则
        nonunit  = True
        for lhs, rules in new_grammar.items():
            for rule in rules:
                if len(get_tokens(rule)) == 1:
                    if not is_terminal(rule):
                        nonunit = False
                        break
            if not nonunit:
                break
        
        # 仍然存在单元推导
        if not nonunit:
            old_grammar = copy.deepcopy(new_grammar)
            new_grammar = defaultdict(list)

    return new_grammar

def eliminate_mixed(grammar):
    '''
    消除终结符与其他符号混合的规则
    eg.
    S -> aA
    ==>
    S -> RA
    R -> 'a'
    '''
    new_grammar = defaultdict(list)
    for lhs, rules in grammar.items():
        for rule in rules:
            regen = []
            tokens = get_tokens(rule)
            if len(tokens) == 1:
                new_grammar[lhs].append(rule)
                continue
            for token in tokens:
                if is_terminal(token):
                    # 消除终结符混合
                    nonterminal = rule_exist(token, new_grammar)
                    if nonterminal:
                        regen.append(nonterminal)
                    else:
                        # 重新生成一个非终结符，推导生成当前终结符
                        new_nonterminal = get_nonterminal()
                        new_grammar[new_nonterminal].append(token)
                        regen.append(new_nonterminal)
                else:
                    regen.append(token)
            
            new_grammar[lhs].append(' '.join(regen))
        
    return new_grammar
                        

def eliminate_multi(grammar):
    '''
    消除2个以上非终结符的推导规则
    eg.
    S -> ABS
    ==>
    S -> RS
    R -> AB
    '''
    old_grammar = copy.deepcopy(grammar)
    new_grammar = defaultdict(list)
    nomulti = False

    # 循环直到没有非终结符大于2的情况
    while not nomulti:
        for lhs, rules in old_grammar.items():
            for rule in rules:
                tokens = get_tokens(rule)
                # 非终结符大于2
                if len(tokens) > 2:
                    # 前n-1个非终结符作为单独推导生成规则
                    without_last = tokens[:-1]
                    without_last_rule = ' '.join(without_last)
                    nonterminal = rule_exist(without_last_rule, new_grammar)
                    if not nonterminal:
                        nonterminal = get_nonterminal()
                        new_grammar[nonterminal].append(without_last_rule)
                    new_rule  = nonterminal + ' ' + tokens[-1]
                    new_grammar[lhs].append(new_rule)
                else:
                    new_grammar[lhs].append(rule)

        # 检查新语法中是否还有不满足的情况
        nomulti  = True
        for lhs, rules in new_grammar.items():
            for rule in rules:
                if len(get_tokens(rule)) > 2:
                    nomulti = False
                    break
            if not nomulti:
                break
        
        # 仍然存在非终结符大于2的规则
        if not nomulti:
            old_grammar = copy.deepcopy(new_grammar)
            new_grammar = defaultdict(list)

    return new_grammar

def cnf2gnf(grammar):
    '''
    将CNF乔姆斯基范式转换成GNF格雷巴赫范式：
    Only a start symbol can generate ε. For example, if S is the start symbol then S -> ε is in GNF.
    A non-terminal can generate a terminal. For example, if A is Non terminal and a is terminal then, A -> a is in GNF.
    A non-terminal can generate a terminal followed by any number of non-terminals. For Example, S -> aAS is in GNF.
    '''
    old_grammar = copy.deepcopy(grammar)
    new_grammar = defaultdict(list)
    isgnf = False

    # 循环直到满足GNF形式
    while not isgnf:
        for lhs, rules in old_grammar.items():
            for rule in rules:
                tokens = get_tokens(rule)
                if len(tokens) == 1 and is_terminal(rule):
                    # 生成终结符
                    new_grammar[lhs].append(rule)
                    continue
                first_token = tokens[0]
                rest_tokens = tokens[1:]
                if not is_terminal(first_token):
                    # 第一个符号不是终结符,将第一个非终结符扩展推导
                    new_rules = []
                    extended_rules = old_grammar[first_token]
                    for extension in extended_rules:
                        temp_rule = rest_tokens[:]
                        temp_rule.insert(0, extension)
                        new_rules.append(temp_rule)
                    for nr in new_rules:
                        new_grammar[lhs].append(' '.join(nr))
                else:
                    # 已经满足要求
                    new_grammar[lhs].append(rule)  


        # 检查新语法中是否还有不满足GNF的情况
        isgnf  = True
        for lhs, rules in new_grammar.items():
            for rule in rules:
                tokens = get_tokens(rule)
                first_token = tokens[0]
                if not is_terminal(first_token):
                    isgnf = False
                    break
            if not isgnf:
                break
        # 仍然不满足GNF
        if not isgnf:
            old_grammar = copy.deepcopy(new_grammar)
            new_grammar = defaultdict(list)

    return new_grammar


def get_tokens(rule):
    '''获取规则中的每个原子符号'''
    pattern = re.compile("([^\s\"\']+)|\"([^\"]*)\"|\'([^\']*)\'")
    return [matched.group(0) for matched in pattern.finditer(rule)]

def is_terminal(unit):
    '''判断是否终结符，终结符被引号包裹'''
    pattern = re.compile("(\'(.*?)\')|(\"(.*?)\")")
    match = pattern.match(unit)
    if match:
        return True
    else:
        return False

def rule_exist(rule, grammar):
    '''
    rule是否已经在生成文法中了
    @return 是则返回生存rule的非终结符，否则返回None
    '''
    for nonterminal, rules in grammar.items():
        if rule in rules:
            return nonterminal
    return None

def strip_chars(rule):
    return rule.strip('\n\t ')

def get_nonterminal():
    '''从非终结符列表里取出一个非终结符，如果列表为空则重新生成'''
    global NONTERMINALS
    if NONTERMINALS:
        return "$" + NONTERMINALS.pop(0) + "#"
    else:
        _gen_nonterminal()
        return "$" + NONTERMINALS.pop(0) + "#"

def _gen_nonterminal():
    '''通过排列组合生成一组非终结符'''
    global COUNT
    global NONTERMINALS
    NONTERMINALS = [''.join(x) for x in list(combinations(ascii_uppercase, COUNT))]
    COUNT += 1

if __name__== "__main__":
    import argparse
    parser = argparse.ArgumentParser(description = 'Script to convert CFG to GNF form')
    parser.add_argument(
            '--grammar',
            type = str,
            required = True,
            help = 'Location of grammar file')
    parser.add_argument(
            '--out',
            type = str,
            required = True,
            help = 'Location of output file')
    parser.add_argument(
            '--start',
            type = str,
            required = True,
            help = 'Start token')
    parser.add_argument(
        '--debug',
        type = bool,
        help = 'Debug mode'
    )
    args = parser.parse_args()

    main(args.grammar, args.out, args.start, args.debug)