grammar Filter;

@header {
package com.bybutter.sisyphus.dsl.filtering.grammar;
}

filter
    : e=expression? EOF
    ;

expression
    : init=sequence (op='AND' seq+=sequence)*
    ;

sequence
    : init=factor e+=factor*
    ;

factor
    : init=condition (op='OR' e+=condition)*
    ;

condition
    : 'NOT' expression  # NotCondition
    | left=member comparator right=value  # CompareCondition
    ;

value
    : member
    | function
    | literal
    ;

function
    : name=IDENTIFIER '(' argList? ')'
    ;

argList
    : args+=value (',' args+=value)*
    ;

member
    : names+=IDENTIFIER ('.' names+=IDENTIFIER)*
    ;

literal
    : sign=MINUS? tok=NUM_INT duration='s'?   # Int
    | tok=NUM_UINT # Uint
    | sign=MINUS? tok=NUM_FLOAT  duration='s'? # Double
    | tok=STRING    # String
    | tok='true'    # BoolTrue
    | tok='false'   # BoolFalse
    | tok='null'    # Null
    | tok=DURATION # Duration
    | tok=TIMESTAMP # Timestamp
    ;

comparator
    : LESS_EQUALS
    | LESS_THAN
    | GREATER_EQUALS
    | GREATER_THAN
    | NOT_EQUALS
    | EQUALS
    | COLON
    ;

// Lexer Rules
// ===========

EQUALS : '=';
NOT_EQUALS : '!=';
LESS_THAN : '<';
LESS_EQUALS : '<=';
GREATER_EQUALS : '>=';
GREATER_THAN : '>';

AND : 'AND';
OR : 'OR';

LBRACKET : '[';
RPRACKET : ']';
LBRACE : '{';
RBRACE : '}';
LPAREN : '(';
RPAREN : ')';
DOT : '.';
COMMA : ',';
MINUS : '-';
EXCLAM : '!';
QUESTIONMARK : '?';
COLON : ':';
PLUS : '+';
STAR : '*';
SLASH : '/';
PERCENT : '%';
TRUE : 'true';
FALSE : 'false';
NULL : 'null';

fragment BACKSLASH : '\\';
fragment LETTER : 'A'..'Z' | 'a'..'z' ;
fragment DIGIT  : '0'..'9' ;
fragment EXPONENT : ('e' | 'E') ( '+' | '-' )? DIGIT+ ;
fragment HEXDIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment ESC_SEQ
    : ESC_CHAR_SEQ
    | ESC_BYTE_SEQ
    | ESC_UNI_SEQ
    | ESC_OCT_SEQ
    ;

fragment ESC_CHAR_SEQ
    : BACKSLASH ('a'|'b'|'f'|'n'|'r'|'t'|'v'|'"'|'\''|'\\'|'?'|'`')
    ;

fragment ESC_OCT_SEQ
    : BACKSLASH ('0'..'3') ('0'..'7') ('0'..'7')
    ;

fragment ESC_BYTE_SEQ
    : BACKSLASH ( 'x' | 'X' ) HEXDIGIT HEXDIGIT
    ;

fragment ESC_UNI_SEQ
    : BACKSLASH 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    | BACKSLASH 'U' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    ;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ -> channel(HIDDEN) ;
COMMENT : '//' (~'\n')* -> channel(HIDDEN) ;

NUM_FLOAT
  : ( DIGIT+ ('.' DIGIT+) EXPONENT?
    | DIGIT+ EXPONENT
    | '.' DIGIT+ EXPONENT?
    )
  ;

NUM_INT
  : ( DIGIT+ | '0x' HEXDIGIT+ );

NUM_UINT
   : DIGIT+ ( 'u' | 'U' )
   | '0x' HEXDIGIT+ ( 'u' | 'U' )
   ;

STRING
  : '"' (ESC_SEQ | ~('\\'|'"'|'\n'|'\r'))* '"'
  | '\'' (ESC_SEQ | ~('\\'|'\''|'\n'|'\r'))* '\''
  ;

DURATION
  : NUM_INT 's'
  | NUM_FLOAT 's'
  ;

TIMESTAMP
  : NUM_INT '-' NUM_INT '-' NUM_INT 'T' NUM_INT ':' NUM_INT ':' (NUM_INT | NUM_FLOAT) 'Z'
  ;

IDENTIFIER : (LETTER | '_') ( LETTER | DIGIT | '_')*;