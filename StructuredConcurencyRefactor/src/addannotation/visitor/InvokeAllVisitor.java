package addannotation.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.DFA.ModeMatchDFA;
import addannotation.DFA.State;
import addannotation.store.CommonRefactorNode;
import addannotation.store.InvokeAllRefactorNode;
import addannotation.store.VerifyIfMatched;

public class InvokeAllVisitor {

	public static String arguementName;
	public static String arguementType;
	public static String iterators;
	public static String iterator;
	public static boolean flag = false;
	public static List<TryStatement> tryList = new ArrayList<>();
	public static void findInvokeAll(MethodDeclaration cuu, InvokeAllRefactorNode refactorNode, ModeMatchDFA dfa) {

		GeneralVisitor.getTryStatement(cuu, tryList);

		for (TryStatement tryStmt : tryList) {
			findInvokeAllInTryStmt(tryStmt, refactorNode);
			findDefinitionWithArguementName(cuu);
			findgetMethodInForStatement(tryStmt,refactorNode,dfa);
		}
		
	}

	public static void findInvokeAllInTryStmt(TryStatement tryStmt, InvokeAllRefactorNode refactorNode) {
		tryStmt.accept(new ASTVisitor() {

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				// TODO Auto-generated method stub

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
				if (fragment.getInitializer() instanceof MethodInvocation) {
					MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
					String invokedMethod = invocation.getName().getIdentifier();
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding != null) {

						ITypeBinding type = methodBinding.getDeclaringClass();
						String invoker = type.getName();
						System.out.println("invoker is :" + invoker);
						System.out.println("invokedMethod is :" + invokedMethod);
						if (invoker.equals("ExecutorService") && invokedMethod.equals("invokeAll")) {

							// 需要将语句的左边保留下来，先将node存下来
							GeneralVisitor.putIntoList(node, refactorNode.invokeAllStmt);
							GeneralVisitor.putIntoList(tryStmt, refactorNode.tryStmt);
							
							// 获取这次参数的名称
							arguementName = invocation.arguments().get(0).toString();
							iterators = fragment.getName().toString();
							System.out.println("arguement is:" + arguementName);
							
						}

					}

				}

				return true;
			}
		});

	}

	public static void findDefinitionWithArguementName(MethodDeclaration cuu) {
		cuu.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationStatement node) {
				// TODO Auto-generated method stub
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
				String variableName = fragment.getName().toString();
				if (variableName.equals(arguementName)) {

					Type type = node.getType();
					if (type instanceof ParameterizedType) {
						ParameterizedType paramType = (ParameterizedType) type;
						List<Type> typeArguments = paramType.typeArguments();
						for (Type typeArgument : typeArguments) {
							System.out.println("TypeArgument Name: " + typeArgument.toString());
							arguementType = typeArgument.toString();
						}

					}

				}

				return true;
			}

		});

	}

	public static void findgetMethodInForStatement(TryStatement cuu, InvokeAllRefactorNode refactorNode,ModeMatchDFA dfa) {

		cuu.accept(new ASTVisitor() {
			@Override
			public boolean visit(EnhancedForStatement node) {
				// TODO Auto-generated method stub
				String iteratorsName = node.getExpression().toString();
				SingleVariableDeclaration singleVarDec = node.getParameter();
				if (singleVarDec != null) {

					String iteratorName = singleVarDec.getName().toString();
					System.out.println("iteratorsName is:" + iteratorsName);
					System.out.println("iteratorName is:" + iteratorName);
					if (iteratorsName.equals(iterators)) {
						iterator = iteratorName;
						Statement statement = node.getBody();

						System.out.println("statement is:" + statement);
						if (hasgetMethodInforStatement(statement, refactorNode)) {
							System.out.println("改变状态为acceptB");
							GeneralVisitor.putIntoList(node, refactorNode.forStmt);
							dfa.state = State.STATEB_ACCCPTED;
							
						} 
					}

				}

				return true;
			}

		});

	}

	public static boolean hasgetMethodInforStatement(Statement cuu, InvokeAllRefactorNode refactorNode) {
		flag = false;
		cuu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				// TODO Auto-generated method stub
				String iteratorName = node.getExpression().toString();
				String methodName = node.getName().toString();
			

				if (iteratorName.equals(iterator) && methodName.equals("get")) {
					System.out.println("methodInvocation iteratorName is: " + iteratorName);

					refactorNode.getMethodInvocation = node;
					flag = true;
				}
				return true;
			}
		});

		return flag;

	}

}
