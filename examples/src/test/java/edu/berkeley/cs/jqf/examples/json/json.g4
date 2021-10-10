JSON: OBJ |
      ARR

OBJ: '{' PAIR '}' |
     '{' PAIR ',' PAIR '}' |
     '{' '}'

PAIR: STRING ':' VALUE

ARR: '[' VALUE ']' |
     '[' VALUE ',' VALUE  ']' |
     '[' ']'

VALUE: STRING |
       INT |
       OBJ |
       ARR |
       'true' |
       'false' |
       'null'

STRING: '"a"' |
        '"b"' |
        '"c"'

NUMBER: '1/2' |
        '1E2' |
        '1E02' |
        '1E+02' |
        '-1' |
        '-1.00' |
        '-1/2' |
        '-1E2' |
        '-1E02' |
        '-1E+02' |
        '1/0' |
        '0/0' |
        '-2147483648/-1' |
        '-9223372036854775808/-1'

INT: '1' |
     '2'

