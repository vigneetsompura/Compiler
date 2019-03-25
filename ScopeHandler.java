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

public class ScopeHandler {

	HashMap<String, Declaration> symbolTable; 
	int currentScope;
	LinkedList<Integer> scopeStack;
	
	public ScopeHandler() {
		super();
		this.symbolTable = new HashMap<String, Declaration>();
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
	
	public void add(String name, Declaration dec) {
		symbolTable.put(name+"@"+currentScope, dec);
	}
	
	public Declaration lookupAll(String name) {
		ListIterator<Integer> listIterator = scopeStack.listIterator();
		while (listIterator.hasNext()) {
			Declaration dec = (Declaration) symbolTable.get(name+"@"+listIterator.next());
			if(dec!=null) {
				return dec;
			}
		}
		return null;
	}
	
	public Declaration lookup(String name) {
		return (Declaration) symbolTable.get(name+"@"+scopeStack.peek());
	}
	
	
	
	
}
