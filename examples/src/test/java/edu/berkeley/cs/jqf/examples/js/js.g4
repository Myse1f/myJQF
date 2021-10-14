JS: STATEMENT

STATEMENT: EXPRESSION |
           BREAK |
           CONTINUE |
           RETURN |
           THROW |
           VAR |
           IF |
           FOR |
           WHILE |
           NAMEDFUNCTION |
           SWITCH |
           TRY |
           BLOCK

EXPRESSION: LITERAL |
            IDENTITY |
            BINARY |
            UNARY |
            TERNARY |
            CALL |
            FUNCTION |
            PROPERTY |
            INDEX |
            ARROWFUNCTION

BREAK: 'break'

CONTINUE: 'continue'

RETURN: 'return' |
        'return' EXPRESSION

THROW: 'throw' EXPRESSION

VAR: 'var' IDENTITY

IF: 'if' '(' EXPRESSION ')' BLOCK |
    'if' '(' EXPRESSION ')' BLOCK 'else' BLOCK

FOR: 'for' '(' EXPRESSION ';' EXPRESSION ';' EXPRESSION ')' BLOCK

WHILE: 'while' '(' EXPRESSION ')' BLOCK

NAMEDFUNCTION: 'function' IDENTITY '(' ARGS ')' BLOCK

ARGS: IDENTITY |
      IDENTITY ',' ARGS

SWITCH: 'switch' '(' EXPRESSION ')' '{' CASES '}'

CASES: 'case' IDENTITY ':' BLOCK

TRY: 'try' BLOCK CATCH

CATCH: 'catch' '(' IDENTITY ')' BLOCK

BLOCK: '{' STATEMENTS '}'

STATEMENTS: STATEMENT |
            STATEMENT ';' STATEMENTS

EXPRESSIONS: EXPRESSION |
             EXPRESSION ',' EXPRESSIONS

ARRAY: '[' EXPRESSIONS ']'

OBJECTPROPERTY: IDENTITY ':' EXPRESSION

OBJECTPROPERTIES: OBJECTPROPERTY |
                 OBJECTPROPERTY ',' OBJECTPROPERTIES

OBJECT: '{' OBJECTPROPERTIES '}'

LITERAL: ARRAY |
         OBJECT |
         INTEGER |
         BOOLEAN |
         STRING |
         'undefined' |
         'null' |
         'this'

INTEGER: '1' |
         '2' |
         '3' |
         '0' |
         '-1' |
         '65536'

BOOLEAN: 'true' |
         'false'

STRING: '"abc"' |
        '"hello world"'

IDENTITY: 'a' |
          'b' |
          'c' |
          'd' |
          'e' |
          'f' |
          'g' |
          'h' |
          'j' |
          'k' |
          'l' |
          'm' |
          'n' |
          'o' |
          'p' |
          'q' |
          'r' |
          's' |
          't' |
          'u' |
          'v' |
          'w' |
          'x' |
          'y' |
          'z'

BINARYTOKEN: '!=' |
             '!==' | 
             '%' | 
             '%=' | 
             '&' | 
             '&&' |  
             '&=' | 
             '*' |
             '*=' |
             '+' | 
             '+=' | 
             '-' |
             '-=' |
             '/' |
             '/=' |
             '<' |
             '<<' |
             '>>=' |
             '<=' |
             '=' |
             '==' |
             '===' |
             '>' |
             '>=' |
             '>>' |
             '>>=' |
             '>>>' |
             '>>>=' |
             '^' |
             '^=' |
             'in' |
             'instanceof'

UNARYTOKEN: '!' |
            '++' |
            '--' |
            '~' |
            'delete' |
            'new' |
            'typeof'

BINARY: IDENTITY ' ' BINARYTOKEN ' ' EXPRESSION

UNARY: UNARYTOKEN ' ' EXPRESSION

TERNARY: IDENTITY '?' EXPRESSION ':' EXPRESSION

CALL: IDENTITY '(' ARGS ')'

FUNCTION: 'function' IDENTITY '(' ARGS ')' BLOCK

PROPERTY: IDENTITY '.' IDENTITY

INDEX: IDENTITY '[' EXPRESSION ']'

ARROWFUNCTION: '(' ARGS ')' '=>' BLOCK |
               '(' ARGS ')' '=>' EXPRESSION

