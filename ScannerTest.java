/**
 * COP 5556: Programming Language Principles
 * Project 6
 * Due Date: November 20, 2018
 * 
 * Name: Vigneet M Sompura
 * UFID: 8121 - 1616
 * Email: vigneetsompura@ufl.edu
 */
/**

 * JUunit tests for the Scanner
 */

package compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import compiler.Scanner.LexicalException;
import compiler.Scanner.Token;

public class ScannerTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}

		/**
		 *Retrieves the next token and checks that it is an EOF token. 
		 *Also checks that this was the last token.
		 *
		 * @param scanner
		 * @return the Token that was retrieved
		 */
		
		Token checkNextIsEOF(Scanner scanner) {
			Scanner.Token token = scanner.nextToken();
			assertEquals(Scanner.Kind.EOF, token.kind);
			assertFalse(scanner.hasTokens());
			return token;
		}


		/**
		 * Retrieves the next token and checks that its kind, position, length, line, and position in line
		 * match the given parameters.
		 * 
		 * @param scanner
		 * @param kind
		 * @param pos
		 * @param length
		 * @param line
		 * @param pos_in_line
		 * @return  the Token that was retrieved
		 */
		Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(pos, t.pos);
			assertEquals(length, t.length);
			assertEquals(line, t.line());
			assertEquals(pos_in_line, t.posInLine());
			return t;
		}

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @param length
		 * @return  the Token that was retrieved
		 */
		Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(length, t.length);
			return t;
		}
		


		/**
		 * Simple test case with an empty program.  The only Token will be the EOF Token.
		 *   
		 * @throws LexicalException
		 */
		@Test
		public void testEmpty() throws LexicalException {
			String input = "12343454636745543441344353456766745456.12334543535";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
		}

		
		@Test
		public void testRandom() throws LexicalException {
			String input = "<<===*****- \n %{c\no\nm\nm\ne\nn\nt%} \n+/%%{**cd-f<=sdf%aa%}";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNext(scanner,Scanner.Kind.OP_LT, 1);  
			checkNext(scanner,Scanner.Kind.OP_LE, 2);
			checkNext(scanner,Scanner.Kind.OP_EQ, 2);
			checkNext(scanner,Scanner.Kind.OP_POWER, 2);
			checkNext(scanner,Scanner.Kind.OP_POWER, 2);
			checkNext(scanner,Scanner.Kind.OP_TIMES, 1);
			checkNext(scanner,Scanner.Kind.OP_MINUS, 1);
			checkNext(scanner,Scanner.Kind.OP_PLUS, 1);
			checkNext(scanner,Scanner.Kind.OP_DIV, 1);
			checkNext(scanner,Scanner.Kind.OP_MOD, 1);
			
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testRandom2() throws LexicalException {
			String input = "\t boolean a = true";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNext(scanner,Scanner.Kind.KW_boolean, 7);  
			checkNext(scanner,Scanner.Kind.IDENTIFIER, 1);
			checkNext(scanner,Scanner.Kind.OP_ASSIGN, 1);
			checkNext(scanner,Scanner.Kind.BOOLEAN_LITERAL, 4);
			checkNextIsEOF(scanner);
		}
		
		/**
		 * This example shows how to test that your scanner is behaving when the
		 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
		 * 
		 * The example shows catching the exception that is thrown by the scanner,
		 * looking at it, and checking its contents before rethrowing it.  If caught
		 * but not rethrown, then JUnit won't get the exception and the test will fail.  
		 * 
		 * The test will work without putting the try-catch block around 
		 * new Scanner(input).scan(); but then you won't be able to check 
		 * or display the thrown exception.
		 * 
		 * @throws LexicalException
		 */
		@Test
		public void failIllegalChar() throws LexicalException {
			String input = ";;~";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new Scanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		@Test
		public void failNumber() throws LexicalException {
			String input = "String a = 'ab'";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new Scanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(13,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		@Test
		public void failIllegalChar2() throws LexicalException {
			String input = "=~";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new Scanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(1,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		@Test
		public void commentWithoutEnd() throws LexicalException {
			String input = "<<===*****-+/%{%{%}";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new Scanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(16,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		/**
		 * Using the two previous functions as a template, you can implement other JUnit test cases.
		 * */
		
}
