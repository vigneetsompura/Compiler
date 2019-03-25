/**
 * COP 5556: Programming Language Principles
 * Project 6
 * Due Date: November 20, 2018
 * 
 * Name: Vigneet M Sompura
 * UFID: 8121 - 1616
 * Email: vigneetsompura@ufl.edu
 */

package compiler;

import java.util.ArrayList;
import java.util.List;

import compiler.AST.*;
import compiler.Scanner.Kind;
import compiler.Scanner.Token;

public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}
	
	/*
	 * Program -> Identifier Block
	 * F: Identifier    P: Identifier
	 */
	public Program program() throws SyntaxException {
		
		Token firstToken = t;
		match(Kind.IDENTIFIER);
		Block block = block();
		return new Program(firstToken, firstToken.val(), block);
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 * F: { P: {
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = { Kind.KW_if, Kind.IDENTIFIER, Kind.KW_sleep, Kind.KW_print, Kind.KW_while/* Complete this */  };
	Kind[] firstDeclB = {Kind.OP_ASSIGN, Kind.COMMA};
	Kind[] firstExpression = {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_EXCLAMATION, Kind.LPAREN};
	Kind[] firstPrimary = {Kind.INTEGER_LITERAL, Kind.BOOLEAN_LITERAL, Kind.FLOAT_LITERAL, Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.LPAREN, Kind.IDENTIFIER};
	Kind[] firstFunctionName = {Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float};
	
	
	public Block block() throws SyntaxException {
		match(Kind.LBRACE);
		Token firstToken = t;
		List<PLPASTNode> DecStat = new ArrayList<PLPASTNode>();
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
	    	DecStat.add(declaration()); 
		} else if (checkKind(firstStatement)) {
			DecStat.add(statement()); 
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		return new Block(firstToken, DecStat);

	}
	
	/*
	 * Declaration ->  Type Identifier DeclB
	 * F: type P: type
	 */
	
	public Declaration declaration() throws SyntaxException {
		Token firstToken = t;
		Kind type = type();
		String name = t.val();
		match(Kind.IDENTIFIER);
		if(checkKind(firstDeclB)) {
			return declB(firstToken, type, name);
		}
		if(checkKind(Kind.IDENTIFIER)) {
			throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Identifier is not allowed here!\n Possible solutions: \n"
					+ "\t1. Separate identifiers with comma\n"
					+ "\t2. If assigning value of a variable to another use = operator\n"
					+ "\t3. Missing semicolon after declaration");
		}else if (checkKind(firstExpression) || checkKind(firstPrimary) || checkKind(firstFunctionName)) {
			throw new SyntaxException (t, t.line()+":"+t.posInLine()+":: assignment operator missing before "+ t.val());
		}else if(!checkKind(Kind.SEMI)) {
			throw new SyntaxException(t,t.line()+":"+t.posInLine() + " :: "+t.val()+" is not valid here! or missing ;" );
		}
		return new VariableDeclaration(firstToken, type, name, null);
	}
	
	/*
	 * DeclB -> = Expression 		F: =  P:  = 
	 * DeclB -> null 					F: null  P: ;
     * DeclB -> , Identifier (, Identifier)*		F: ,   P: , ;
	 */
	public Declaration declB(Token firstToken, Kind type, String name) throws SyntaxException {
		if(checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			Expression expression = expression();
			return new VariableDeclaration(firstToken, type, name, expression);
		}else {
			List<String> names = new ArrayList<String>();
			names.add(name);
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				names.add(t.val());
				match(Kind.IDENTIFIER);
			}
			return new VariableListDeclaration(firstToken, type, names);
		}
	}
	
	/*
	 * Type -> int | float | boolean | char | string
	 */
	public Kind type() throws SyntaxException {
		if(checkKind(firstDec)) {
			Kind type = t.kind;
			t = scanner.nextToken();
			return type;
		}else {
			throw new SyntaxException(t, "Datatype(i.e. int, float, boolean, char, string) is expected here!");
		}
	}
	
	/* 
	 * Statement -> IfStatement  			F: if P: if
     * Statement -> AssignmentStatement		F: Identifier P: Identifier
     * Statement -> SleepStatement 			F: sleep P: sleep
     * Statement -> PrintStatement			F: print P: print
     * Statement -> WhileStatment			F: while P: while
     */
	public Statement statement() throws SyntaxException {
		switch(t.kind) {
			case KW_if:
				return ifstatement();
			case KW_print:
				return printstatement();
			case KW_sleep:
				return sleepstatement();
			case KW_while:
				return whilestatement();
			case IDENTIFIER:
				return assignmentstatement();
		default:
			break;
		}
		return null;
	}
	
	/* 
	 * IfStatement -> if ( Expression ) Block			F: if P: if
     */
	public IfStatement ifstatement() throws SyntaxException {
		Token firstToken = t;
		match(Kind.KW_if);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block block = block();
		return new IfStatement(firstToken, condition, block);
	}
	
	/* 
	 * WhileStatement -> while ( Expression ) Block	F: while P: while
     */
	public WhileStatement whilestatement() throws SyntaxException {
		Token firstToken = t;
		match(Kind.KW_while);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block block = block();
		return new WhileStatement(firstToken, condition, block);
	}
	
	/* 
	 * AssignmentStatement -> Identifier = Expression   	F: Identifier P: Identifier
     */
	public AssignmentStatement assignmentstatement() throws SyntaxException{
		Token firstToken = t;
		String identifier = t.val();
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Expression expression = expression();
		return new AssignmentStatement(firstToken, new LHS(firstToken, identifier), expression);
	}
	
	/* 
	 * SleepStatement -> sleep Expression			F: sleep P: sleep
     */
	public SleepStatement sleepstatement() throws SyntaxException {
		Token firstToken = t;
		match(Kind.KW_sleep);
		Expression expression = expression();
		return new SleepStatement(firstToken,expression);
	}
	
	/* 
	 * PrintStatement -> print Expression			F: print P: print
     */
	public PrintStatement printstatement() throws SyntaxException {
		Token firstToken = t;
		match(Kind.KW_print);
		Expression expression = expression();
		return new PrintStatement(firstToken,expression);
	}
	
	
	/*
	 * Expression -> OrExpression ExprA		F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname 
	 */
	public Expression expression() throws SyntaxException{
		Token firstToken = t;
		Expression firstExpression = orexpression();
		if(checkKind(Kind.OP_QUESTION)) {
			return exprA(firstToken, firstExpression);
		}
		return firstExpression;
	}
	
	/* 
	 * ExprA -> ? Expression : Expression	F: ? P: ? 
	 * ExprA -> null				F: null P: :, ), ;
	 */
	public Expression exprA(Token firstToken, Expression condition) throws SyntaxException {
		match(Kind.OP_QUESTION);
		Expression trueExpression = expression();
		match(Kind.OP_COLON);
		Expression falseExpression = expression();
		return new ExpressionConditional(firstToken, condition, trueExpression, falseExpression);
	}
	
	/* 
	 * OrExpression -> AndExpression ( | AndExpression )* 	
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname  
	 */
	public Expression orexpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = andexpression();
		while(checkKind(Kind.OP_OR)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = andexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	
	/* 
	 * AndExpression -> EqExpression ( & EqExpression )*		
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression andexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = eqexpression();
		while(checkKind(Kind.OP_AND)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = eqexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
			
	/* 
	 * EqExpression -> RelExpression ( ( == | != ) RelExpression )* 	
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression eqexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = relexpression();
		while(checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = relexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/* 
	 * RelExpression -> AddExpression ( ( < | > | <= | >= ) AddExpression )*		
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression relexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = addexpression();
		while(checkKind(Kind.OP_LT) || checkKind(Kind.OP_GT) || checkKind(Kind.OP_LE) || checkKind(Kind.OP_GE)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = addexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/* 
	 * AddExpression -> MultExpression ( ( + | - ) MultExpression )*		
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression addexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = mulexpression();
		while(checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = mulexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 *  MultExpression -> PowerExpression ( ( * | / | % ) PowerExpression )* 	
	 *  F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression mulexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = powerexpression();
		while(checkKind(Kind.OP_TIMES) || checkKind(Kind.OP_DIV) || checkKind(Kind.OP_MOD)) {
			Kind op = t.kind;
			t = scanner.nextToken();
			Expression rightExpression = powerexpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/* 
	 * PowerExpression -> UnaryExpression ( ** PowerExpression |  null )	
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression powerexpression() throws SyntaxException{
		Token firstToken = t;
		Expression leftExpression = unaryexpression();
		if(checkKind(Kind.OP_POWER)) {
			t = scanner.nextToken();
			Expression rightExpression = powerexpression();
			return new ExpressionBinary(firstToken, leftExpression, Kind.OP_POWER, rightExpression);
		}
		return leftExpression;
	}
	
	/* 
	 * UnaryExpression -> + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary	
	 * F: +,-,!,primary,(, fname  P: +,-,!,primary,(, fname
	 */
	public Expression unaryexpression() throws SyntaxException{
		if(checkKind(firstPrimary) || checkKind(firstFunctionName)) {
			return primary();
		}else if(checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_EXCLAMATION) ) {
			Token firstToken = t;
			Kind op = firstToken.kind;
			t = scanner.nextToken();
			Expression expression = unaryexpression();
			return new ExpressionUnary(firstToken,op,expression);
		}else {
			throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: "+ t.val()+" is not valid here! +, -, !, (, literal, identifier or function required.");
		}
	}
	
	/* 
	 * Primary -> INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | CHAR_LITERAL 
	 * 			| STRING_LITERAL | ( Expression ) | IDENTIFIER | Function
	 * F: (,primary,fname  P: primary,(, fname
	 */
	public Expression primary() throws SyntaxException{
		Token firstToken = t;
		if(checkKind(firstFunctionName)) {
			return function();
		}else if(checkKind(Kind.LPAREN)){
			match(Kind.LPAREN);
			Expression expression = expression();
			match(Kind.RPAREN);
			return expression;
		}else {
			
			switch (firstToken.kind){
				case INTEGER_LITERAL:
					t = scanner.nextToken();
					return new ExpressionIntegerLiteral(firstToken, Integer.parseInt(firstToken.val()));
				case BOOLEAN_LITERAL:
					t = scanner.nextToken();
					return new ExpressionBooleanLiteral(firstToken, Boolean.parseBoolean(firstToken.val()));
				case FLOAT_LITERAL:
					t = scanner.nextToken();
					return new ExpressionFloatLiteral(firstToken, Float.parseFloat(firstToken.val()));
				case CHAR_LITERAL:
					t = scanner.nextToken();
					String charval = firstToken.val();
					char charvalue = '\0'; 
					if(charval.charAt(1)!='\'') {
						charvalue = charval.charAt(1);
					}
					return new ExpressionCharLiteral(firstToken, charvalue);
				case STRING_LITERAL:
					t = scanner.nextToken();
					String strval = firstToken.val().substring(1, firstToken.length-1);
					return new ExpressionStringLiteral(firstToken, strval);
				case IDENTIFIER:
					t = scanner.nextToken();
					return new ExpressionIdentifier(firstToken, firstToken.val());
			default:
				break;
					
			}
			
		}
		return null;
	}
	
	/* 
	 * Function -> FunctionName ( Expression )		
	 * F: fname  P: fname
	 */
	public FunctionWithArg function() throws SyntaxException{
		Token firstToken = t;
		Kind kind = functionname();
		match(Kind.LPAREN);
		Expression expression = expression();
		match(Kind.RPAREN);
		return new FunctionWithArg(firstToken, kind, expression);
	}
	
	/* 
	 * FunctionName -> sin | cos | atan | abs | log | int | float	
	 * F: fname  P: fname
	 */
	public Kind functionname() throws SyntaxException{
		Token firstToken = t;
		t = scanner.nextToken();
		return firstToken.kind;
	}
	
	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		} else {
			if(checkKind(Kind.SEMI)) {
				throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Program block should not end with semi colon!");
			}else {
				throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Program block should not be followed by "+t.val());
			}
		}
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (checkKind(kind)) {
			t = scanner.nextToken();
		}
		else {
			switch (kind){
				case IDENTIFIER:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Identifier required here! "+t.val()+ " is not a valid Identifier");
				case LBRACE:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: { required here! Instead found "+t.val());
				case LPAREN:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: ( required here! Instead found "+t.val());
				case RPAREN:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: ) required here! Instead found "+t.val());
				case SEMI:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: semi colon expeted here! Instead found "+t.val());
				case OP_COLON:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Colon expeted here! Instead found "+t.val());
				case RBRACE:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: "+t.val()+" is not allowed here! If the program block ends here, } is missing!");
				default:
					throw new SyntaxException(t, t.line()+":"+t.posInLine()+" :: Invalid Syntax! ");
			}
		}
	}

}
