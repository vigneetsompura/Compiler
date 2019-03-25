package compiler.AST;

import compiler.Scanner.Token;

public abstract class Statement extends PLPASTNode {
	
	public Statement(Token firstToken) {
		super(firstToken);
	}

}
