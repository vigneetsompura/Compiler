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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import compiler.AST.Declaration;
import compiler.AST.ExpressionIdentifier;
import compiler.Scanner.Token;

public class SlotHandler {
	
	@SuppressWarnings("serial")
	public static class UndefinedVariableException extends Exception {
		Token t;

		public UndefinedVariableException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	HashMap<String, Integer> slotTable; 
	public HashMap<String, Boolean> initialization; 
	int currentScope;
	LinkedList<Integer> scopeStack;
	
	public SlotHandler() {
		super();
		this.slotTable = new HashMap<String, Integer>();
		this.initialization = new HashMap<String, Boolean>();
		currentScope = 0;
		this.scopeStack = new LinkedList<Integer>();
		
	}
	
	public void enterScope() {
		currentScope++;
		scopeStack.push(currentScope);
	}
	
	public void closeScope() {
		scopeStack.pop();
	}
	
	public void add(String name) {
		slotTable.put(name+"@"+currentScope, slotTable.size()+1);
		initialization.put(name+"@"+currentScope, false);
	}
	
	public void initialize(String name) {
		String key = lookupLatestKey(name);
		initialization.put(key, true);
	}
	
	public Integer lookupAll(String name) throws Exception{
		ListIterator<Integer> listIterator = scopeStack.listIterator();
		while (listIterator.hasNext()) {
			int scope = listIterator.next();
			Integer slot = slotTable.get(name+"@"+scope);
			if(slot != null) {
				if(initialization.get(name+"@"+scope)) {
					return slot;
				}else {
					throw new Error("The local variable "+name+" may not have been initialized\n");
				}
			}
		}
		return null;
	}
	
	public Integer lookupAll(ExpressionIdentifier expressionIdent) throws UndefinedVariableException{
		String name = expressionIdent.name;
		ListIterator<Integer> listIterator = scopeStack.listIterator();
		while (listIterator.hasNext()) {
			int scope = listIterator.next();
			Integer slot = slotTable.get(name+"@"+scope);
			if(slot != null) {
				if(initialization.get(name+"@"+scope)) {
					return slot;
				}else {
					throw new UndefinedVariableException(expressionIdent.firstToken, "The local variable "+name+" may not have been initialized\n");
				}
			}
		}
		return null;
	}
	
	public String lookupLatestKey(String name) {
		ListIterator<Integer> listIterator = scopeStack.listIterator();
		while (listIterator.hasNext()) {
			Integer scope = listIterator.next();
			Integer slot = slotTable.get(name+"@"+scope);
			if(slot != null) {
				return name+"@"+scope;
			}
		}
		return null;
	}
	
	public Integer lookup(String name) {
		return slotTable.get(name+"@"+scopeStack.peek());
	}
	
	
	
	
}
