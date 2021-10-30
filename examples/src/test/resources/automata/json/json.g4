JSON: OBJ |
      ARR

OBJ: '{' PAIRS '}'

PAIR: STRING ':' VALUE

PAIRS: PAIR |
       PAIR ',' PAIR |
       PAIR ',' PAIR ',' PAIR |
       PAIR ',' PAIR ',' PAIR ',' PAIR |
       PAIR ',' PAIR ',' PAIR ',' PAIR ',' PAIR

ARR: '[' VALUES ']'

VALUE: STRING |
       INT |
       NUMBER |
       OBJ |
       ARR |
       'true' |
       'false' |
       'null'

VALUES: VALUE |
        VALUE ',' VALUE |
        VALUE ',' VALUE ',' VALUE |
        VALUE ',' VALUE ',' VALUE ',' VALUE |
        VALUE ',' VALUE ',' VALUE ',' VALUE ',' VALUE

STRING: '$$STRING$$'

NUMBER: '$$NUMBER$$'

INT: '$$INTEGER$$'

