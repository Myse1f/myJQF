JSON: OBJ |
      ARR

OBJ: '{' PAIRS '}' |
     '{' '}'

PAIR: STRING ':' VALUE

PAIRS: PAIR |
       PAIR ',' PAIRS

ARR: '[' VALUES ']' |
     '[' ']'

VALUE: STRING |
       INT |
       NUMBER |
       OBJ |
       ARR |
       'true' |
       'false' |
       'null'

VALUES: VALUE |
        VALUE ',' VALUES

STRING: '$$STRING$$'

NUMBER: '$$NUMBER$$'

INT: '$$INTEGER$$'

