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

import compiler.Scanner.Kind;

public class Types {
	
	public static enum Type {
		INTEGER, BOOLEAN, FLOAT, CHAR, STRING, NONE;
	}
	
	public static Type getType(Kind kind) {
		switch(kind) {
		case KW_int: {
			return Type.INTEGER;
		}
		case KW_float: {
			return Type.FLOAT;
		}
		case KW_boolean: {
			return Type.BOOLEAN;
		}
		case KW_char: {
			return Type.CHAR;
		}
		case KW_string: {
			return Type.STRING;
		}
		default:
			break;		
		}
		// should not reach here
		assert false: "invoked getType with Kind that is not a type"; 
		return null;
		
	}

}
