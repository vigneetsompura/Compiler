package compiler.AST;

import compiler.Scanner.Token;
import compiler.Types.Type;;

public abstract class Expression extends PLPASTNode {

	public Type type;
	
	public Expression(Token firstToken) {
		super(firstToken);
	}	
	
	public Type getType() {
		return this.type;
	}
}
