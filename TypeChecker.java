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

import java.util.Arrays;
import java.util.HashSet;

import compiler.AST.AssignmentStatement;
import compiler.AST.Block;
import compiler.AST.Declaration;
import compiler.AST.Expression;
import compiler.AST.ExpressionBinary;
import compiler.AST.ExpressionBooleanLiteral;
import compiler.AST.ExpressionCharLiteral;
import compiler.AST.ExpressionConditional;
import compiler.AST.ExpressionFloatLiteral;
import compiler.AST.ExpressionIdentifier;
import compiler.AST.ExpressionIntegerLiteral;
import compiler.AST.ExpressionStringLiteral;
import compiler.AST.ExpressionUnary;
import compiler.AST.FunctionWithArg;
import compiler.AST.IfStatement;
import compiler.AST.LHS;
import compiler.AST.PLPASTNode;
import compiler.AST.PLPASTVisitor;
import compiler.AST.PrintStatement;
import compiler.AST.Program;
import compiler.AST.SleepStatement;
import compiler.AST.VariableDeclaration;
import compiler.AST.VariableListDeclaration;
import compiler.AST.WhileStatement;
import compiler.Scanner.Kind;
import compiler.Scanner.Token;
import compiler.Types.Type;

public class TypeChecker implements PLPASTVisitor {
	
	TypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		
		ScopeHandler scope = new ScopeHandler();
		scope.add(program.name, null);
		program.block.visit(this, scope);
		return null;
	}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		ScopeHandler scope = (ScopeHandler)arg;
		scope.enterScope();
		for(PLPASTNode DeclStat: block.declarationsAndStatements){
			scope = (ScopeHandler) DeclStat.visit(this, scope);
		}
		scope.closeScope();
		return scope;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		ScopeHandler scope = (ScopeHandler) arg;
		if(scope.lookup(declaration.name) == null) {
			if(declaration.expression == null) {
				scope.add(declaration.name, declaration);
				return scope;
			}else {
				Type t = (Type) declaration.expression.visit(this, arg);
				if(t == Types.getType(declaration.type)) {
					scope.add(declaration.name, declaration);
					return scope;
				}else {
					throw new SemanticException(declaration.expression.firstToken, "Expression should be of type "+ Types.getType(declaration.type).toString()+ " instead found "+t.toString());
				}
			}
		}else {
			throw new SemanticException(declaration.firstToken, "Variable "+declaration.name+" can not be declared twice in the same scope!");
		}
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		ScopeHandler scope = (ScopeHandler) arg;
		for(String name: declaration.names) {
			if(scope.lookup(name) == null) {
				scope.add(name, declaration);
			}else {
				throw new SemanticException(declaration.firstToken, "Variable "+name+" can not be declared twice in the same scope!");
			}
		}
		return scope;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return expressionBooleanLiteral.type;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Type t1 = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type t2 = (Type) expressionBinary.rightExpression.visit(this, arg);
		Kind op = expressionBinary.op;
		Type t = inferType(t1,op,t2);
		if(t!=null) {
			expressionBinary.type = inferType(t1,op,t2);
			return expressionBinary.type;
		}else {
			throw new SemanticException(expressionBinary.firstToken, "Operation "+op.toString()+" not supported between "+t1.toString()+ " and "+ t2.toString());
		}
		
	}

	private Type inferType(Type t1, Kind op, Type t2) throws SemanticException{
		HashSet<Kind> arith = new HashSet<Kind>(Arrays.asList(new Kind[] {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_TIMES, Kind.OP_DIV, Kind.OP_POWER}));
		HashSet<Kind> logic = new HashSet<Kind>(Arrays.asList(new Kind[] {Kind.OP_AND, Kind.OP_OR}));
		HashSet<Kind> cond = new HashSet<Kind>(Arrays.asList(new Kind[] {Kind.OP_EQ, Kind.OP_NEQ,Kind.OP_GT, Kind.OP_LT, Kind.OP_LE, Kind.OP_GE}));
		if(t1 == Type.INTEGER) {
			if(t2 == Type.INTEGER) {
				if(arith.contains(op) || logic.contains(op) || op==Kind.OP_MOD) {
					return Type.INTEGER;
				}else if (cond.contains(op)) {
					return Type.BOOLEAN;
				}
			}else if (t2 == Type.FLOAT) {
				if(arith.contains(op)) {
					return Type.FLOAT;
				}
			}
		}else if (t1 == Type.FLOAT) {
			if(t2==Type.FLOAT) {
				if(arith.contains(op)) {
					return Type.FLOAT;
				}else if(cond.contains(op)) {
					return Type.BOOLEAN;
				}
			}else if(t2 == Type.INTEGER) {
				if(arith.contains(op)) {
					return Type.FLOAT;
				}
			}
		}else if (t1 == Type.STRING && t2 == Type.STRING && op == Kind.OP_PLUS) {
			return Type.STRING;
		}else if (t1 == Type.BOOLEAN && t2 == Type.BOOLEAN) {
			if(cond.contains(op)||logic.contains(op)) {
				return Type.BOOLEAN;
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Type e0 = (Type) expressionConditional.condition.visit(this, arg);
		Type e1 = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type e2 = (Type) expressionConditional.falseExpression.visit(this, arg);
		
		if(e0 == Type.BOOLEAN) {
			
			if(e1==e2) {
			expressionConditional.type = e1;
			return expressionConditional.type;
			}else {
				throw new SemanticException(expressionConditional.falseExpression.firstToken, "Both true and false expression must be of same type!");
			}
		}else {
			throw new SemanticException(expressionConditional.condition.firstToken, "Condition Statement must be of type Boolean!");
		}
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = Type.FLOAT;
		return expressionFloatLiteral.type;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		FunctionWithArg.type = inferTypeFuncWithArgs(FunctionWithArg);
		return FunctionWithArg.type;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		ScopeHandler scope = (ScopeHandler) arg;
		String name = expressionIdent.name;
		expressionIdent.dec = scope.lookupAll(name);
		if(expressionIdent.dec != null) {
			if(expressionIdent.dec.getClass()==VariableDeclaration.class) {
				VariableDeclaration vd = (VariableDeclaration) expressionIdent.dec;
				expressionIdent.type = Types.getType(vd.type);
				return expressionIdent.type;
			}else if (expressionIdent.dec.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration vd = (VariableListDeclaration) expressionIdent.dec;
				expressionIdent.type = Types.getType(vd.type);
				return expressionIdent.type;
			}else {
				throw new SemanticException(expressionIdent.firstToken, "Invalid Variable");
			}
		}else {
			throw new SemanticException(expressionIdent.firstToken, "Variable "+name+ " is not defined!");
		}
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Type.INTEGER;
		return expressionIntegerLiteral.type;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		expressionStringLiteral.type = Type.STRING;
		return expressionStringLiteral.type;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.type = Type.CHAR;
		return expressionCharLiteral.type;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		Type l = (Type) statementAssign.lhs.visit(this, arg);
		Type e = (Type) statementAssign.expression.visit(this, arg);
		if(l==e) {
			return arg;
		}else {
			throw new SemanticException(statementAssign.expression.firstToken, "Expression should be of Type "+l.toString()+" instead found "+ e.toString());
		}
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ScopeHandler scope = (ScopeHandler) arg;
		Type t = (Type) ifStatement.condition.visit(this, arg);
		if(t== Type.BOOLEAN) {
			return ifStatement.block.visit(this, scope);
		}else {
			throw new SemanticException(ifStatement.condition.firstToken, "Condtion should be of type BOOLEAN instead found "+ t.toString());
		}
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		ScopeHandler scope = (ScopeHandler) arg;
		Type t = (Type) whileStatement.condition.visit(this, arg);
		if(t== Type.BOOLEAN) {
			return whileStatement.b.visit(this, scope);
		}else {
			throw new SemanticException(whileStatement.condition.firstToken, "Condtion should be of type BOOLEAN instead found "+ t.toString());
		}
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		Type t =(Type) printStatement.expression.visit(this, arg);
		switch(t) {
		case INTEGER:
		case BOOLEAN:
		case FLOAT:
		case CHAR:
		case STRING:
			return arg;
		default:
			throw new SemanticException(printStatement.expression.firstToken, "Cannot print expression of type "+t.toString());
		}
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Type t = (Type) sleepStatement.time.visit(this, arg);
		if(t == Type.INTEGER) {
			return arg;
		}else {
			throw new SemanticException(sleepStatement.time.firstToken, "Expression should be of type INTEGER!");
		}
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Kind op = expressionUnary.op;
		Type e = (Type) expressionUnary.expression.visit(this, arg);
		if(op == Kind.OP_EXCLAMATION) {
			if(e==Type.INTEGER || e == Type.BOOLEAN) {
				expressionUnary.type = e;
				return e;
			}else {
				throw new SemanticException(expressionUnary.expression.firstToken, "! can only be used with INTEGER or BOOLEAN!");
			}
		}else if(op==Kind.OP_PLUS || op==Kind.OP_MINUS){
			if(e==Type.INTEGER || e == Type.FLOAT) {
				expressionUnary.type = e;
				return e;
			}else {
				throw new SemanticException(expressionUnary.expression.firstToken, "+ or - can only be used with INTEGER or FLOAT!");
			}
		}else {
			throw new SemanticException(expressionUnary.firstToken, "Invalid Operation!");
		}
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		String name = lhs.identifier;
		ScopeHandler scope = (ScopeHandler) arg;
		lhs.dec = (Declaration) scope.lookupAll(name);
		if(lhs.dec != null) {
			if(lhs.dec.getClass()==VariableDeclaration.class) {
				VariableDeclaration vd = (VariableDeclaration) lhs.dec;
				lhs.type = Types.getType(vd.type);
				return lhs.type;
			}else if (lhs.dec.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration vd = (VariableListDeclaration) lhs.dec;
				lhs.type = Types.getType(vd.type);
				return lhs.type;
			}else {
				throw new SemanticException(lhs.firstToken, "Invalid Variable");
			}
		}else {
			throw new SemanticException(lhs.firstToken, "Variable "+name+ " is not defined!");
		}
		
	}
	
	public Types.Type inferTypeFuncWithArgs(FunctionWithArg functionWithArg) throws SemanticException{
		Kind functionName = functionWithArg.functionName;
		Expression e = functionWithArg.expression;
		
		if(e.type == Type.INTEGER) {
			if(functionName == Kind.KW_abs) {
				return Type.INTEGER;
			}else if(functionName == Kind.KW_float) {
				return Type.FLOAT;
			}else if (functionName == Kind.KW_int) {
				return Type.INTEGER;
			}else {
				throw new SemanticException(e.firstToken, functionName.toString()+" cannot be used with "+e.type.toString()+" arguments");
			}
		}else if(e.type == Type.FLOAT) {
			if(functionName == Kind.KW_float) {
				return Type.FLOAT;
			}else if(functionName == Kind.KW_int) {
				return Type.INTEGER;
			}else {
				return Type.FLOAT;
			}
		}else {
			throw new SemanticException(e.firstToken, functionName.toString()+" cannot be used with "+e.type.toString()+" arguments");
		}
	}

}
