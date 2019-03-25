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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import compiler.Scanner;
import compiler.AST.PLPASTVisitor;
import compiler.AST.Program;
import compiler.TypeChecker.SemanticException;

public class TypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {int a; a = !2;}";
		typeCheck(input);
	}
	
	@Test
	public void test1() throws Exception {
		String input = "prog {int a,b; a=10; if(a<10) {int c=10; a=b;}; while(a<=10) {print(float(b));};}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { print(true+4); }"; //should throw an error due to incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

}
