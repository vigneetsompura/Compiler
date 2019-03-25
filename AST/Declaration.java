package compiler.AST;

import compiler.Scanner.Token;

public abstract class Declaration extends PLPASTNode {

	public Declaration(Token firstToken) {
		super(firstToken);
	}
}
