grammar Filter;

@header {
package com.bybutter.sisyphus.dsl.filtering.grammar;
}

// Filter is composed of one or more whitespace (WS) separated expressions.
//
// A sequence expresses a logical relationship between 'expressions' where
// the ranking of a filter result may be scored according to the number
// expressions that match and other such criteria as the proximity of expressions
// to each other within a document.
//
// When filters are used with exact match semantics rather than fuzzy
// match semantics, a sequence is equivalent to AND.
//
// Example: `New York Giants AND Yankees`
//
// The expression `New York (Giants AND Yankees)` is equivalent to the
// example.
filter
    : expression* EOF
    ;

// Expressions may either be a conjunction (AND) of sequences or a simple
// factor.
//
// Note, the AND is case-sensitive.
//
// Example: `a AND b OR c AND d`
//
// The expression `a AND (b OR c) AND d` is equivalent to the example.
expression
    : factor (op='AND' factor)*
    ;

// Factors may either be a disjunction (OR) of terms or a simple term.
//
// Note, the OR is case-sensitive.
//
// Example: `a < 10 OR a >= 100`
factor
    : term (op='OR' term)*
    ;

// Terms may either be unary or simple expressions.
//
// Unary expressions negate the simple expression, either mathematically `-`
// or logically `NOT`. The negation styles may be used interchangeably.
//
// Note, the `NOT` is case-sensitive and must be followed by at least one
// whitespace (WS).
//
// Examples:
// * logical not     : `NOT (a OR b)`
// * alternative not : `-file:".java"`
// * negation        : `-30`
term
    : ('NOT' | '-')? simple
    ;

// Simple expressions may either be a restriction or a nested (composite)
// expression.
simple
    : restriction
    | composite
    ;

// Restrictions express a relationship between a comparable value and a
// single argument. When the restriction only specifies a comparable
// without an operator, this is a global restriction.
//
// Note, restrictions are not whitespace sensitive.
//
// Examples:
// * equality         : `package=com.google`
// * inequality       : `msg != 'hello'`
// * greater than     : `1 > 0`
// * greater or equal : `2.5 >= 2.4`
// * less than        : `yesterday < request.time`
// * less or equal    : `experiment.rollout <= cohort(request.user)`
// * has              : `map:key`
// * global           : `prod`
//
// In addition to the global, equality, and ordering operators, filters
// also support the has (`:`) operator. The has operator is unique in
// that it can test for presence or value based on the proto3 type of
// the `comparable` value. The has operator is useful for validating the
// structure and contents of complex values.
restriction
    : comparable (comparator arg)?
    ;

// Comparable may either be a member or function.
comparable
    : member
    | function
    ;

// Member expressions are either value or DOT qualified field references.
//
// Example: `expr.type_map.1.type`
member
    : IDENTIFIER ('.' field)*
    ;

// Function calls may use simple or qualified names with zero or more
// arguments.
//
// All functions declared within the list filter, apart from the special
// `arguments` function must be provided by the host service.
//
// Examples:
// * `regex(m.key, '^.*prod.*$')`
// * `math.mem('30mb')`
//
// Antipattern: simple and qualified function names may include keywords:
// NOT, AND, OR. It is not recommended that any of these names be used
// within functions exposed by a service that supports list filters.
function
    : name ('.' name)* '(' argList? ')'
    ;

// Comparators supported by list filters.
comparator
    : LESS_EQUALS      // <=
    | LESS_THAN        // <
    | GREATER_EQUALS   // >=
    | GREATER_THAN     // >
    | NOT_EQUALS       // !=
    | EQUALS           // =
    | COLON            // :
    ;

// terms or clarify operator precedence.
//
// Example: `(msg.endsWith('world') AND retries < 10)`
composite
    : '(' expression ')'
    ;

// Value may either be a TEXT or STRING.
//
// TEXT is a free-form set of characters without whitespace (WS)
// or . (DOT) within it. The text may represent a variable, string,
// number, boolean, or alternative literal value and must be handled
// in a manner consistent with the service's intention.
//
// STRING is a quoted string which may or may not contain a special
// wildcard `*` character at the beginning or end of the string to
// indicate a prefix or suffix-based search within a restriction.
value
    : tok=NUM_INT duration='s'?    # Int
    | tok=NUM_FLOAT  duration='s'? # Double
    | tok=NUM_UINT                 # Uint
    | tok=STRING                   # String
    | tok=DURATION                 # Duration
    | tok=TIMESTAMP                # Timestamp
    | tok='true'                   # BoolTrue
    | tok='false'                  # BoolFalse
    | tok='null'                   # Null
    ;

// Fields may be either a value or a keyword.
field
    : IDENTIFIER
    | NUM_INT
    | keyword
    ;

// Names may either be TEXT or a keyword.
name
    : IDENTIFIER
    | keyword
    ;

argList
    : arg (',' arg)*
    ;

arg
    : comparable
    | composite
    | value
    ;

keyword
    : 'NOT'
    | 'AND'
    | 'OR'
    | 'true'
    | 'false'
    | 'null'
    ;

// Lexer Rules
// ===========

EQUALS : '=';
NOT_EQUALS : '!=';
LESS_THAN : '<';
LESS_EQUALS : '<=';
GREATER_EQUALS : '>=';
GREATER_THAN : '>';

NOT : 'NOT';
AND : 'AND';
OR : 'OR';

TRUE: 'true';
FALSE: 'false';
NULL: 'null';

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

NUM_FLOAT
  : ( DIGIT+ ('.' DIGIT+) EXPONENT?
    | DIGIT+ EXPONENT
    )
  ;

NUM_INT
  : ( DIGIT+ | '0x' HEXDIGIT+ );

NUM_UINT
   : DIGIT+ ( 'u' | 'U' )
   | '0x' HEXDIGIT+ ( 'u' | 'U' )
   ;

IDENTIFIER : (LETTER | '_') ( LETTER | DIGIT | '_')*;