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

import org.objectweb.asm.Opcodes;

import compiler.AST.AssignmentStatement;
import compiler.AST.Block;
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
import compiler.Types.Type;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class CodeGen implements PLPASTVisitor, Opcodes {

	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	MethodVisitor mv;

	final boolean DEVEL;
	final boolean GRADE;
	

	public CodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		SlotHandler slots = (SlotHandler) arg;
		slots.enterScope();
		for (PLPASTNode node : block.declarationsAndStatements) {
			slots = (SlotHandler) node.visit(this, slots);
		}
		slots.closeScope();
		return slots;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

		mv.visitCode();
		
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		SlotHandler slots = new SlotHandler();
		program.block.visit(this, slots);

		CodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		mv.visitInsn(RETURN);

		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitMaxs(0, 0);

		mv.visitEnd();
		cw.visitEnd();
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		SlotHandler slots = (SlotHandler) arg;
			slots.add(declaration.name);
			String name = declaration.name;
			Type t = Types.getType(declaration.type);
			switch(t) {
			case INTEGER:
				if(declaration.expression!=null) {
					slots.initialize(declaration.name);
					declaration.expression.visit(this, arg);
					mv.visitVarInsn(ISTORE, slots.lookupAll(name));
				}
				break;
			case FLOAT:
				if(declaration.expression!=null) {
					slots.initialize(declaration.name);
					declaration.expression.visit(this, arg);
					mv.visitVarInsn(FSTORE, slots.lookupAll(name));
				}
				break;
			case CHAR:
				if(declaration.expression!=null) {
					slots.initialize(declaration.name);
					declaration.expression.visit(this, arg);
					mv.visitVarInsn(ISTORE, slots.lookupAll(name));
				}

				break;
			case BOOLEAN:
				if(declaration.expression!=null) {
					slots.initialize(declaration.name);
					declaration.expression.visit(this, arg);
					mv.visitVarInsn(ISTORE, slots.lookupAll(name));
				}
				break;
			case STRING:
				if(declaration.expression!=null) {
					slots.initialize(declaration.name);
					declaration.expression.visit(this, arg);
					mv.visitVarInsn(ASTORE, slots.lookupAll(name));
				}
				break;
			default:
				break;
			}
		
		return slots;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {

		SlotHandler slots = (SlotHandler) arg;
		for(String name: declaration.names) {
			slots.add(name);
		}
		return slots;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Expression left = expressionBinary.leftExpression;
		Expression right = expressionBinary.rightExpression;
		Label l1,l2;
		switch(expressionBinary.op) {
		
		case OP_AND:
			left.visit(this, arg);
			right.visit(this, arg);
			mv.visitInsn(IAND);
			break;
		case OP_DIV:
			left.visit(this, arg);
			if(left.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			right.visit(this, arg);
			if(right.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			mv.visitInsn(FDIV);
			if(left.getType()==Type.INTEGER && right.getType()==Type.INTEGER)
				mv.visitInsn(F2I);
			break;
		case OP_EQ:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case OP_GE:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLT, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPGE, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case OP_GT:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLE, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPGT, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case OP_LE:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFGT, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPLE, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case OP_LT:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFGE, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPLT, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			
			break;
		case OP_MINUS:
			left.visit(this, arg);
			if(left.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			right.visit(this, arg);
			if(right.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			mv.visitInsn(FSUB);
			if(left.getType()==Type.INTEGER && right.getType()==Type.INTEGER)
				mv.visitInsn(F2I);
			break;
		case OP_MOD:
			left.visit(this,arg);
			right.visit(this, arg);
			mv.visitInsn(IREM);
			break;
		case OP_NEQ:
			left.visit(this, arg);
			right.visit(this, arg);
			l1 = new Label();
			l2 = new Label();
			if(left.getType()==Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}else {
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case OP_OR:
			left.visit(this, arg);
			right.visit(this, arg);
			mv.visitInsn(IOR);
			break;
		case OP_PLUS:
			if(left.getType()==Type.STRING && right.getType()==Type.STRING) {
				mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
				mv.visitInsn(Opcodes.DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
				left.visit(this, arg);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				
				right.visit(this, arg);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			}else {
				left.visit(this, arg);
				if(left.getType()==Type.INTEGER)
					mv.visitInsn(I2F);
				right.visit(this, arg);
				if(right.getType()==Type.INTEGER)
					mv.visitInsn(I2F);
				mv.visitInsn(FADD);
				if(left.getType()==Type.INTEGER && right.getType()==Type.INTEGER)
					mv.visitInsn(F2I);
			}
			
			break;
		case OP_POWER:
			left.visit(this, arg);
			if(left.getType()==Type.INTEGER)
				mv.visitInsn(I2D);
			else if(left.getType()==Type.FLOAT)
				mv.visitInsn(F2D);
			
			right.visit(this, arg);
			if(right.getType()==Type.INTEGER)
				mv.visitInsn(I2D);
			else if(right.getType()==Type.FLOAT)
				mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
			if(left.getType()==Type.FLOAT || right.getType()==Type.FLOAT)
				mv.visitInsn(D2F);
			else
				mv.visitInsn(D2I);
			break;
		case OP_TIMES:
			left.visit(this, arg);
			if(left.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			right.visit(this, arg);
			if(right.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			mv.visitInsn(FMUL);
			if(left.getType()==Type.INTEGER && right.getType()==Type.INTEGER)
				mv.visitInsn(F2I);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		switch(FunctionWithArg.functionName) {
		case KW_sin:
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			break;
		case KW_cos:
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			break;
		case KW_atan:
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
			break;
		case KW_abs:
			switch(FunctionWithArg.expression.getType()) {
			case INTEGER:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
				break;
			case FLOAT:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
				break;
			default:
				break;
			}
			break;
		case KW_log:
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(D2F);
			break;
		case KW_int:
			if(FunctionWithArg.expression.getType()==Type.FLOAT)
				mv.visitInsn(F2I);
			break;
		case KW_float:
			if(FunctionWithArg.expression.getType()==Type.INTEGER)
				mv.visitInsn(I2F);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		SlotHandler slots = (SlotHandler) arg; 
		Type t = expressionIdent.getType();
		switch(t) {
		case INTEGER:
			mv.visitVarInsn(ILOAD, slots.lookupAll(expressionIdent));
			break;
		case FLOAT:
			mv.visitVarInsn(FLOAD, slots.lookupAll(expressionIdent));
			break;
		case BOOLEAN:
			mv.visitVarInsn(ILOAD, slots.lookupAll(expressionIdent));
			break;
		case CHAR:
			mv.visitVarInsn(ILOAD, slots.lookupAll(expressionIdent));
			break;
		case STRING:
			mv.visitVarInsn(ALOAD, slots.lookupAll(expressionIdent));
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.expression.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return arg;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		SlotHandler slots = (SlotHandler) arg;
		slots.initialize(lhs.identifier);
		switch(lhs.type) {
		case INTEGER:
			mv.visitVarInsn(ISTORE, slots.lookupAll(lhs.identifier));
			break;
		case BOOLEAN:
			mv.visitVarInsn(ISTORE, slots.lookupAll(lhs.identifier));
			break;
		case CHAR:
			mv.visitVarInsn(ISTORE, slots.lookupAll(lhs.identifier));
			break;
		case FLOAT:
			mv.visitVarInsn(FSTORE, slots.lookupAll(lhs.identifier));
			break;
		case STRING:
			mv.visitVarInsn(ASTORE, slots.lookupAll(lhs.identifier));
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Label l = new Label();
		ifStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFEQ, l);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(l);
		return arg;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitLabel(l1);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFEQ, l2);
		whileStatement.b.visit(this, arg);
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);
		return arg;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
		case INTEGER : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
		}
		break;
		
		case FLOAT : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break;
		
		case CHAR : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break;
		
		case STRING : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		break;
		default:
			break;
		}
		return arg;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return arg;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Expression expr = expressionUnary.expression;
		switch(expressionUnary.op) {
			case OP_MINUS:
				expr.visit(this, arg);
				if(expr.getType()==Type.FLOAT)
					mv.visitInsn(FNEG);
				else
					mv.visitInsn(INEG);
				break;
			case OP_PLUS:
				expr.visit(this, arg);
				break;
			case OP_EXCLAMATION:
				expr.visit(this, arg);
				if(expr.getType()==Type.INTEGER) {
					mv.visitInsn(ICONST_M1);
					mv.visitInsn(IXOR);
				}else if(expr.getType() == Type.BOOLEAN) {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				}
				break;
		default:
			break;
		}
		
		return null;
	}

}
