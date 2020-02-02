# Compiler for WinZigC
A compiler for the mini-language WinZigC.

Done as a part of the module CS4542 - Compiler Design

##Tokens of the Language
|   |   |
|---|---|
|   \n 			| Newline|
|	program		| Start of Program|
|	var			| Variable|
|	const		| Constant|
|	type		| To define a data type|
|	function	| To define a function|
|	return		| return from function|
|	begin		| start of a block|
|	end			| end of a block|
|	:=:			| swap|
|	:=			| assignment operator|
|	output		| output an expression or string|
|	if			| keyword|
|	then		| keyword|
|	else		| keyword|
|	while		| keyword for loop|
|	do			| keyword for loop|
|	case		| keyword|
|	of			| keyword|
|	..			| dots for case expression|
|	otherwise	| keyword|
|	repeat		| keyword for repeat-until loop|
|	for			| keyword for loop|
|	until		| keyword for repeat-until loop|
|	loop		| keyword for loop-pool loop|
|	pool		| keyword for loop-pool loop|
|	exit		| keyword|
|	<=			| less than equal to binary operator|
|	<>			| not equal to binary operator|
|	<			| less than binary operator|
|	\>=			| greater than equal to binary operator|
|	\>			| greater than binary operator|
|	=			| equal to binary operator|
|	mod			| modulus binary operator|
|	and			| and binary operator|
|	or			| or binary operator|
|	not			| not unary operator|
|	read		| read an identifier|
|	succ		| successor of an ordinal value|
|	pred		| predecessor of a ordinal value|
|	chr			| keyword for character function|
|	ord			| keyword for ordinal function|
|	eof			| keyword for end of file|
|	{			| begin of a block|
|	:			| colon|
|	;			| semi colon|
|	.			| single dot|
|	,			| comma|
|	(			| opening bracket|
|	)			| closing bracket|
|	\+			| plus|
|	\-			| minus|
|	\*			| multiply|
|	/			| divide|


##Grammar Rules
|   |   |   |
|---|---|---|
|Winzig|-> 'program' Name ':' Consts Types Dclns SubProgs Body Name '.'|=> "program"|
|Consts|-> 'const' Const (',', Const)* ';'|=> "consts"|
| |->|=> "consts"|
|Const|-> Name '=' ConstValue|=> "const"|
|ConstValue|-> '\<integer>'| |
| |-> '\<char>'| |
| |-> Name| |
|Types|-> 'type' (Type ';')+|=> "types"|
| |->|=> "types"|
|Type|-> Name '=' LitList|=> "type"|
|LitList|-> '(' Name (',', Name)* ')'|=> "lit"|
|SubProgs|-> Fcn*|=> "subprogs"|
|Fcn|-> 'function' Name '(' Params ')' ':' Name ';' Consts Types Dclns Body Name ';'|=> "fcn"|
|Params|-> Dcln (';', Dcln)*|=> "params"|
|Dclns|->'var' (Dcln ';')+|=> "dclns"|
| |->|=> "dclns"|
|Dcln|-> Name (',' Name)* ':' Name|=> "var"|
|Body|-> 'begin' Statement (';', Statement)* 'end'|=> "block"|
|Statement|-> Assignment| |
| |-> 'output' '(' OutExp (',' OutExp)* ')'|=> "output"|
| |-> 'if' Expression 'then' Statement ('else' Statement)?|=> "if"|
| |-> 'while' Expression 'do' Statement|=> "while"|
| |-> 'repeat' Statement (';' Statement)* 'until' Expression|=> "repeat"|
| |-> 'for' '(' ForStat ';' ForExp ';' ForStat ')' Statement|=> "for"|
| |-> 'loop' Statement (';' Statement)* 'pool'|=> "loop"|
| |-> 'case' Expression 'of' Caseclauses OtherwiseClause 'end'|=> "case"|
| |-> 'read' '(' Name (',' Name)* ')'|=> "read"|
| |-> 'exit'|=> "exit"|
| |-> 'return' Expression |=> "return"|
| |-> Body| |
| |-> |=> "\<null>"|
|OutExp|-> Expression|=> "integer"|
| |-> StringNode|=> "string"|
|StringNode|-> '\<string>'| |
|Caseclauses|-> (Caseclause ';')+| |
|Caseclause|-> CaseExpression (',', CaseExpression)* ':' Statement|=> "case_clause"|
|CaseExpression|-> ConstValue| |
| |-> ConstValue '..' ConstValue|=> ".."|
|OtherwiseClause|->'otherwise' Statement|=> "otherwise"|
| |->| |
|Assignment|-> Name ':=' Expression|=> "assign"|
| |-> Name ':=:' Name|=> "swap"|
|ForStat|-> Assignment| |
| |->|=> "\<null>"|
|ForExp|-> Expression| |
| |->|=> "true"|
|Expression|-> Term| |
| |-> Term '<=' Term|=> "<="|
| |-> Term '<' Term|=> "<"|
| |-> Term '>=' Term|=> ">="|
| |-> Term '>' Term|=> ">"|
| |-> Term '=' Term|=> "="|
| |-> Term '<>' Term|=> "<>"
|Term|-> Factor| |
| |-> Term '+' Factor|=> "+"|
| |-> Term '-' Factor|=> "-"|
| |-> Term 'or' Factor|=> "or"|
|Factor|-> Factor '\*' Primary|=> "*"|
| |-> Factor '/' Primary|=> "/"|
| |-> Factor 'and' Primary|=> "and"|
| |-> Factor 'mod' Primary|=> "mod"|
| |-> Primary| |
|Primary|-> '-' Primary|=> "-"|
| |-> '+' Primary| |
| |-> 'not' Primary|=> "not"|
| |-> 'eof'|=> "eof"|
| |-> Name| |
| |-> '\<integer>'| |
| |-> '\<char>'| |
| |-> Name '(' Expression (',' Expression)* ')'|=> "call"|
| |-> '(' Expression ')'| |
| |-> 'succ' '(' Expression ')'|=> "succ"|
| |-> 'pred' '(' Expression ')'|=> "pred"|
| |-> 'chr' '(' Expression ')'|=> "chr"|
| |-> 'ord' '(' Expression ')'|=> "ord"|
|Name|-> '\<identifier>'| |
