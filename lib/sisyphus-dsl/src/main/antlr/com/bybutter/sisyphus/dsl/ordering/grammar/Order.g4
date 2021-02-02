grammar Order;

@header {
package com.bybutter.sisyphus.dsl.ordering.grammar;
}

// Grammar Rules
// =============

start
    : expr? EOF
    ;

expr
    : order ( ',' order )*
    ;

order
    : field ('desc' | 'asc')?
    ;

field
    : IDENTIFIER ( '.' IDENTIFIER )*
    ;

// Lexer Rules
// ===========

DOT : '.';
COMMA : ',';
DESC : 'desc';
ASC : 'asc';

fragment LETTER : 'A'..'Z' | 'a'..'z' ;
fragment DIGIT  : '0'..'9' ;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ -> channel(HIDDEN) ;
COMMENT : '//' (~'\n')* -> channel(HIDDEN) ;

IDENTIFIER : (LETTER | '_') ( LETTER | DIGIT | '_')*;