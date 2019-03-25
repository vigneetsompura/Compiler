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

import static compiler.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import compiler.Scanner;
import compiler.AST.*;
import compiler.Parser.SyntaxException;
import compiler.Scanner.Kind;
import compiler.Scanner.LexicalException;

public class ParserTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static final boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}


		//creates and returns a parser for the given input.
		private Parser makeParser(String input) throws LexicalException {
			show(input);        //Display the input 
			Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			Parser parser = new Parser(scanner);
			return parser;
		}
		
		/**
		 * Test case with an empty program.  This throws an exception 
		 * because it lacks an identifier and a block
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  //The input is the empty string.  
			thrown.expect(SyntaxException.class);
			Parser parser = makeParser(input);
			@SuppressWarnings("unused")
			Program p = parser.parse();
		}
		
		/**
		 * Smallest legal program.
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testSmallest() throws LexicalException, SyntaxException {
			String input = "b{}";  
			Parser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			assertEquals("b", p.name);
			assertEquals(0, p.block.declarationsAndStatements.size());
		}	
		
		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param name
		 * @return
		 */
		Declaration checkDec(Block block, int index, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.name);
			return dec;
		}	
		
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int i; char c;}";
			Parser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "i");
			checkDec(p.block, 1, Kind.KW_char, "c");
		}
		
		
		/** 
		 * Test a specific grammar element by calling a corresponding parser method rather than parse.
		 * This requires that the methods are visible (not private). 
		 * 
		 * @throws LexicalException
		 * @throws SyntaxException
		 */
		
		
		@Test
		public void testExpression() throws LexicalException, SyntaxException {
			String input = "x + 2 - 3";
			Parser parser = makeParser(input);
			Expression e = parser.expression();  //call expression here instead of parse
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary ex = (ExpressionBinary) e;
			assertEquals(ExpressionBinary.class, ex.leftExpression.getClass());
			
			ExpressionBinary b =(ExpressionBinary) ex.leftExpression;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("x", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(2, right.value);
			assertEquals(OP_PLUS, b.op);
			
			assertEquals(ExpressionIntegerLiteral.class, ex.rightExpression.getClass());
			ExpressionIntegerLiteral exr = (ExpressionIntegerLiteral) ex.rightExpression;
			assertEquals(3, exr.value);
			assertEquals(OP_MINUS, ex.op);
			
		}
		
		@Test
		public void testEverything() throws LexicalException, SyntaxException{
			String input = "b{\n" + 
					"	string a,b,c;\n" + 
					"	int x = 2+3;\n" + 
					"	x = (2+3)*4+3**4/((9-sin(a))*y);\n" + 
					"	a = -b;\n" + 
					"	if(p == true | false & true){\n" + 
					"		print(x<=y);\n" + 
					"		sleep(x);\n" + 
					"	};\n" + 
					"	while(c == 'c'){\n" + 
					"		a = x? 2.3 : \"hello\";\n" + 
					"	};\n" + 
					"}";
			Parser parser = makeParser(input);
			
			//b{ block}
			Program p = parser.parse();
			assertEquals("b", p.name);
			assertEquals(6, p.block.declarationsAndStatements.size());
			List<PLPASTNode> stmt = p.block.declarationsAndStatements;
			
			//string a,b,c;
			assertEquals(VariableListDeclaration.class, stmt.get(0).getClass());
			VariableListDeclaration l1 = (VariableListDeclaration) stmt.get(0);
			assertEquals(Kind.KW_string, l1.type);
			assertEquals(3, l1.names.size());
			assertEquals("a", l1.names.get(0));
			assertEquals("b", l1.names.get(1));
			assertEquals("c", l1.names.get(2));
			
			//int x = 2+3;
			assertEquals(VariableDeclaration.class, stmt.get(1).getClass());
			VariableDeclaration l2 = (VariableDeclaration) stmt.get(1);
			assertEquals(Kind.KW_int, l2.type);
			assertEquals("x", l2.name);
			assertEquals(ExpressionBinary.class, l2.expression.getClass());
			ExpressionBinary l2e = (ExpressionBinary) l2.expression;
			assertEquals(ExpressionIntegerLiteral.class, l2e.leftExpression.getClass());
			ExpressionIntegerLiteral l2el = (ExpressionIntegerLiteral) l2e.leftExpression;
			assertEquals(2, l2el.value);
			assertEquals(Kind.OP_PLUS, l2e.op);
			assertEquals(ExpressionIntegerLiteral.class, l2e.rightExpression.getClass());
			ExpressionIntegerLiteral l2er = (ExpressionIntegerLiteral) l2e.rightExpression;
			assertEquals(3, l2er.value);
			
			//x = (2+3)*4+3**4/((9-sin(a))*y);
			assertEquals(AssignmentStatement.class, stmt.get(2).getClass());
			AssignmentStatement l3 = (AssignmentStatement) stmt.get(2);
			assertEquals("x", l3.lhs.identifier);
			assertEquals(ExpressionBinary.class, l3.expression.getClass());
			ExpressionBinary l3e = (ExpressionBinary) l3.expression;
			assertEquals(ExpressionBinary.class, l3e.leftExpression.getClass());
				ExpressionBinary l3el = (ExpressionBinary) l3e.leftExpression;
				assertEquals(ExpressionBinary.class, l3el.leftExpression.getClass());
					ExpressionBinary l3ell = (ExpressionBinary) l3el.leftExpression;
					assertEquals(ExpressionIntegerLiteral.class, l3ell.leftExpression.getClass());
						ExpressionIntegerLiteral l3elll = (ExpressionIntegerLiteral) l3ell.leftExpression;
						assertEquals(2, l3elll.value);
					assertEquals(Kind.OP_PLUS, l3ell.op);
					assertEquals(ExpressionIntegerLiteral.class, l3ell.rightExpression.getClass());
						ExpressionIntegerLiteral l3ellr = (ExpressionIntegerLiteral) l3ell.rightExpression;
						assertEquals(3, l3ellr.value);
				assertEquals(Kind.OP_TIMES, l3el.op);
				assertEquals(ExpressionIntegerLiteral.class, l3el.rightExpression.getClass());
					ExpressionIntegerLiteral l3elr = (ExpressionIntegerLiteral) l3el.rightExpression;
					assertEquals(4, l3elr.value);
			assertEquals(Kind.OP_PLUS, l3e.op);
			assertEquals(ExpressionBinary.class, l3e.rightExpression.getClass());
				ExpressionBinary l3er = (ExpressionBinary) l3e.rightExpression;
				assertEquals(ExpressionBinary.class, l3er.leftExpression.getClass());
					ExpressionBinary l3erl = (ExpressionBinary) l3er.leftExpression;
					assertEquals(ExpressionIntegerLiteral.class, l3erl.leftExpression.getClass());
						ExpressionIntegerLiteral l3erll = (ExpressionIntegerLiteral) l3erl.leftExpression;
						assertEquals(3, l3erll.value);
					assertEquals(Kind.OP_POWER, l3erl.op);
					assertEquals(ExpressionIntegerLiteral.class, l3erl.rightExpression.getClass());
						ExpressionIntegerLiteral l3erlr = (ExpressionIntegerLiteral) l3erl.rightExpression;
						assertEquals(4, l3erlr.value);
				assertEquals(Kind.OP_DIV, l3er.op);
				assertEquals(ExpressionBinary.class, l3er.rightExpression.getClass());
					ExpressionBinary l3err = (ExpressionBinary) l3er.rightExpression;
					assertEquals(ExpressionBinary.class, l3err.leftExpression.getClass());
						ExpressionBinary l3errl = (ExpressionBinary) l3err.leftExpression;
						assertEquals(ExpressionIntegerLiteral.class, l3errl.leftExpression.getClass());
							ExpressionIntegerLiteral l3errll = (ExpressionIntegerLiteral) l3errl.leftExpression;
							assertEquals(9,l3errll.value);
						assertEquals(Kind.OP_MINUS, l3errl.op);
						assertEquals(FunctionWithArg.class, l3errl.rightExpression.getClass());
							FunctionWithArg l3errlr = (FunctionWithArg) l3errl.rightExpression;
							assertEquals(Kind.KW_sin, l3errlr.functionName);
							assertEquals(ExpressionIdentifier.class, l3errlr.expression.getClass());
								ExpressionIdentifier l3errlre = (ExpressionIdentifier) l3errlr.expression;
								assertEquals("a", l3errlre.name);
					assertEquals(Kind.OP_TIMES, l3err.op);
					assertEquals(ExpressionIdentifier.class, l3err.rightExpression.getClass());
						ExpressionIdentifier l3errr = (ExpressionIdentifier) l3err.rightExpression;
						assertEquals("y", l3errr.name);
			
			//a = -b;
			assertEquals(AssignmentStatement.class, stmt.get(3).getClass());
			AssignmentStatement l4 = (AssignmentStatement) stmt.get(3);
			assertEquals("a", l4.lhs.identifier);
			assertEquals(ExpressionUnary.class, l4.expression.getClass());
				ExpressionUnary l4e = (ExpressionUnary) l4.expression;
				assertEquals(Kind.OP_MINUS, l4e.op);
				assertEquals(ExpressionIdentifier.class, l4e.expression.getClass());
					ExpressionIdentifier l4ee = (ExpressionIdentifier) l4e.expression;
					assertEquals("b", l4ee.name);
					
			//if(condition){Block};
			assertEquals(IfStatement.class, stmt.get(4).getClass());
			IfStatement l5 = (IfStatement) stmt.get(4);
			assertEquals(ExpressionBinary.class, l5.condition.getClass());
				ExpressionBinary l5c = (ExpressionBinary) l5.condition;
				assertEquals(ExpressionBinary.class, l5c.leftExpression.getClass());
					ExpressionBinary l5cl = (ExpressionBinary) l5c.leftExpression;
					assertEquals(ExpressionIdentifier.class, l5cl.leftExpression.getClass());
						ExpressionIdentifier l5cll = (ExpressionIdentifier) l5cl.leftExpression;
						assertEquals("p", l5cll.name);
					assertEquals(Kind.OP_EQ, l5cl.op);
					assertEquals(ExpressionBooleanLiteral.class, l5cl.rightExpression.getClass());
						ExpressionBooleanLiteral l5clr = (ExpressionBooleanLiteral) l5cl.rightExpression;
						assertEquals(true, l5clr.value);
				assertEquals(Kind.OP_OR, l5c.op);
				assertEquals(ExpressionBinary.class, l5c.rightExpression.getClass());
					ExpressionBinary l5cr = (ExpressionBinary) l5c.rightExpression;
					assertEquals(ExpressionBooleanLiteral.class, l5cr.leftExpression.getClass());
						ExpressionBooleanLiteral l5crl = (ExpressionBooleanLiteral) l5cr.leftExpression;
						assertEquals(false, l5crl.value);
					assertEquals(Kind.OP_AND, l5cr.op);
					assertEquals(ExpressionBooleanLiteral.class, l5cr.rightExpression.getClass());
						ExpressionBooleanLiteral l5crr = (ExpressionBooleanLiteral) l5cr.rightExpression;
						assertEquals(true, l5crr.value);
			assertEquals(2, l5.block.declarationsAndStatements.size());
				List<PLPASTNode> l5b = l5.block.declarationsAndStatements;
				//print(x<=y)
				assertEquals(PrintStatement.class, l5b.get(0).getClass());
				PrintStatement l5b1 = (PrintStatement) l5b.get(0);
				assertEquals(ExpressionBinary.class, l5b1.expression.getClass());
					ExpressionBinary l5b1e = (ExpressionBinary) l5b1.expression;
					assertEquals(ExpressionIdentifier.class, l5b1e.leftExpression.getClass());
						ExpressionIdentifier l5b1el = (ExpressionIdentifier) l5b1e.leftExpression;
						assertEquals("x", l5b1el.name);
					assertEquals(Kind.OP_LE, l5b1e.op);
					assertEquals(ExpressionIdentifier.class, l5b1e.rightExpression.getClass());
						ExpressionIdentifier l5b1er= (ExpressionIdentifier) l5b1e.rightExpression;
						assertEquals("y", l5b1er.name);
				//sleep(x)
				assertEquals(SleepStatement.class, l5b.get(1).getClass());
				SleepStatement l5b2 = (SleepStatement) l5b.get(1);
				assertEquals(ExpressionIdentifier.class, l5b2.time.getClass());
					ExpressionIdentifier l5b2t = (ExpressionIdentifier) l5b2.time;
					assertEquals("x", l5b2t.name);
			
			//while(condition){block}
			assertEquals(WhileStatement.class, stmt.get(5).getClass());
			WhileStatement l6 = (WhileStatement) stmt.get(5);
			assertEquals(ExpressionBinary.class, l6.condition.getClass());
				ExpressionBinary l6c = (ExpressionBinary) l6.condition;
				assertEquals(ExpressionIdentifier.class, l6c.leftExpression.getClass());
					ExpressionIdentifier l6cl = (ExpressionIdentifier) l6c.leftExpression;
					assertEquals("c", l6cl.name);
				assertEquals(Kind.OP_EQ, l6c.op);
				assertEquals(ExpressionCharLiteral.class, l6c.rightExpression.getClass());
					ExpressionCharLiteral l6cr = (ExpressionCharLiteral) l6c.rightExpression;
					assertEquals('c', l6cr.text);
			assertEquals(1, l6.b.declarationsAndStatements.size());
			List<PLPASTNode> l6b = l6.b.declarationsAndStatements;
			assertEquals(AssignmentStatement.class, l6b.get(0).getClass());
			AssignmentStatement l6b1 = (AssignmentStatement) l6b.get(0);
			assertEquals("a", l6b1.lhs.identifier);
			assertEquals(ExpressionConditional.class, l6b1.expression.getClass());
				ExpressionConditional l6b1e = (ExpressionConditional) l6b1.expression;
				assertEquals(ExpressionIdentifier.class, l6b1e.condition.getClass());
					ExpressionIdentifier l6b1ec = (ExpressionIdentifier) l6b1e.condition;
					assertEquals("x", l6b1ec.name);
				assertEquals(ExpressionFloatLiteral.class, l6b1e.trueExpression.getClass());
					ExpressionFloatLiteral l6b1et = (ExpressionFloatLiteral) l6b1e.trueExpression;
					assertEquals(2.3f, l6b1et.value, 0.0000001);
				assertEquals(ExpressionStringLiteral.class, l6b1e.falseExpression.getClass());
					ExpressionStringLiteral l6b1ef = (ExpressionStringLiteral) l6b1e.falseExpression;
					assertEquals("hello", l6b1ef.text);
		}
		

}
