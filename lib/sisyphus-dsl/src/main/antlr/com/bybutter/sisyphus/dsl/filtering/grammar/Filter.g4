grammar Filter;

@header {
package com.bybutter.sisyphus.dsl.filtering.grammar;
}

filter
    : e=expression? EOF
    ;

// Expressions may either be a conjunction (AND) of sequences or a simple
// sequence.
//
// Note, the AND is case-sensitive.
//
// Example: `a b AND c AND d`
//
// The expression `(a b) AND c AND d` is equivalent to the example.
expression
    : seq+=sequence (op='AND' seq+=sequence)*
    ;

// Sequence is composed of one or more whitespace (WS) separated factors.
//
// A sequence expresses a logical relationship between 'factors' where
// the ranking of a filter result may be scored according to the number
// factors that match and other such criteria as the proximity of factors
// to each other within a document.
//
// When filters are used with exact match semantics rather than fuzzy
// match semantics, a sequence is equivalent to AND.
//
// Example: `New York Giants OR Yankees`
//
// The expression `New York (Giants OR Yankees)` is equivalent to the
// example.
sequence
    : e+=factor+
    ;

// Factors may either be a disjunction (OR) of terms or a simple term.
//
// Note, the OR is case-sensitive.
//
// Example: `a < 10 OR a >= 100`
factor
    : e+=term (op='OR' e+=term)*
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
    : op=('NOT' | '-')? simple
    ;

// Simple expressions may either be a restriction or a nested (composite)
// expression.
simple
    : restriction # RestrictionExpr
    | composite   # CompositeExpr
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
    : left=comparable (op=comparator right=arg)?
    ;

// Comparable may either be a member or function.
comparable
    : function # FucntionExpr
    | member   # MemberExpr
    ;

// Member expressions are either value or DOT qualified field references.
//
// Example: expr.type_map.1.type
member
    : value ('.' e+=field)*
    ;

// Function calls may use simple or qualified names with zero or more
// arguments.
//
// All functions declared within the list filter, apart from the special
// `arguments` function must be provided by the host service.
//
// Examples:
// * regex(m.key, '^.*prod.*$')
// * math.mem('30mb')
//
// Antipattern: simple and qualified function names may include keywords:
// NOT, AND, OR. It is not recommended that any of these names be used
// within functions exposed by a service that supports list filters.
function
    : n+=name ('.' n+=name)* '(' argList? ')'
    ;

// Comparators supported by list filters.
comparator
    : LESS_EQUALS
    | LESS_THAN
    | GREATER_EQUALS
    | GREATER_THAN
    | NOT_EQUALS
    | EQUALS
    | HAS
    ;

// Composite is a parenthesized expression, commonly used to group
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
    : TEXT
    | STRING
    ;

// Fields may be either a value or a keyword.
field
    : value
    | keyword
    ;

name
    : TEXT
    | keyword
    ;

argList
    : e+=arg (',' e+=arg)*
    ;

arg
    : comparable # ArgComparableExpr
    | composite  # ArgCompositeExpr
    ;

keyword
    : NOT
    | AND
    | OR
    ;

// Lexer Rules
// ===========

EQUALS : '=';
NOT_EQUALS : '!=';
IN: 'in';
LESS_THAN : '<';
LESS_EQUALS : '<=';
GREATER_EQUALS : '>=';
GREATER_THAN : '>';

LPAREN : '(';
RPAREN : ')';
DOT : '.';
COMMA : ',';
MINUS : '-';
EXCLAM : '!';
QUESTIONMARK : '?';
HAS : ':';
PLUS : '+';
STAR : '*';
SLASH : '/';
PERCENT : '%';
AND : 'AND';
OR : 'OR';
NOT : 'NOT';

fragment BACKSLASH : '\\';
fragment LETTER : 'A'..'Z' | 'a'..'z' ;
fragment DIGIT  : '0'..'9' ;

WHITESPACE : ('\t' | ' ' | '\r' | '\n'| '\u000C')+ -> channel(HIDDEN) ;
COMMENT : '//' (~'\n')* -> channel(HIDDEN) ;

STRING
  : '"' ~('"'|'\n'|'\r')* '"'
  | '\'' ~('\''|'\n'|'\r')* '\''
  | '"""' .*? '"""'
  | '\'\'\'' .*? '\'\'\''
  ;

TEXT : (LETTER | DIGIT | '_')+;