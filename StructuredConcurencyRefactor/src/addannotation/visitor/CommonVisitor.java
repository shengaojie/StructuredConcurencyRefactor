package addannotation.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.DFA.ModeMatchDFA;
import addannotation.DFA.State;
import addannotation.store.CommonRefactorNode;
import addannotation.store.VerifyIfMatched;
import addannotation.utils.MinOrMax;

@SuppressWarnings("unused")
public class CommonVisitor {
	public static List<CommonRefactorNode> refactorNodes = new ArrayList<>();

	

	public static void collectFutures(ASTNode cuu,CommonRefactorNode refactorNode,ModeMatchDFA dfa) {

		cuu.accept(new ASTVisitor() {

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				// TODO Auto-generated method stub
				VariableDeclarationStatement varStmt = (VariableDeclarationStatement) node;
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varStmt.fragments().get(0); // 存储所有的定义future的信息
				String futureName = fragment.getName().getIdentifier();
				System.out.println("vaiableName is : " + futureName);

				if (fragment.getInitializer() instanceof MethodInvocation) {
					MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding != null) {

						ITypeBinding type = methodBinding.getDeclaringClass();
						String declareClass = type.getName();
						String methodName = methodBinding.getName();
						String retureType = methodBinding.getReturnType().getName();

						if (declareClass.equals("ExecutorService") && methodName.equals("submit")
								&& retureType.contains("Future")) {

						
							GeneralVisitor.putIntoExecutorMap(refactorNode.executorMap);
							GeneralVisitor.putIntoMap(node, refactorNode.futureMap);
							
							VerifyIfMatched.futureList.add(futureName);
							dfa.state = State.StateA;
						
	
						}
					}

				}
				return true;
			}

		});
		

	}
	
	
	public static void collectBetweenSubmitAndGet(MethodDeclaration cuu, CommonRefactorNode refactorNode) {
		List<Statement> statements = cuu.getBody().statements();
		Integer maxKey = MinOrMax.getMaxKey(refactorNode.futureMap);
		System.out.println("maxkey is :" + maxKey);
		Integer minkey = MinOrMax.getMinKey(refactorNode.getMap);

		System.out.println("minKey is:" + minkey);

		for (int i = maxKey + 1; i < minkey; i++) {
			refactorNode.stmtbeSubAndGet.add(statements.get(i));

		}
	}

	public static void collectBetweenGetAndInvoke(MethodDeclaration cuu, CommonRefactorNode refactorNode) {
		List<Statement> statements = cuu.getBody().statements();
		Integer maxKey = MinOrMax.getMaxKey(refactorNode.getMap);
		System.out.println("maxkey is :" + maxKey);
		Integer minkey = MinOrMax.getMinKey(refactorNode.invokeMap);

		System.out.println("minKey is:" + minkey);

		for (int i = maxKey + 1; i < minkey; i++) {
			refactorNode.stmtbeGetAndInvoke.add(statements.get(i));

		}
	}

	public static void collectGetMethod(MethodDeclaration cuu,CommonRefactorNode refactorNode,ModeMatchDFA dfa) {

		cuu.accept(new ASTVisitor() {

			@Override
			public boolean visit(VariableDeclarationStatement node) {
				// TODO Auto-generated method stub
				VariableDeclarationStatement varStmt = (VariableDeclarationStatement) node;

				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
				if (fragment != null) {
					// 获取到如String str = future1.get() 中的str
					String valueName = fragment.getName().toString();
					if (fragment.getInitializer() instanceof MethodInvocation) {
						MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
						String getInvoker = invocation.getExpression().toString();
						String methodInvoke = invocation.getName().toString();

						if (methodInvoke.equals("get") && VerifyIfMatched.futureList.contains(getInvoker)) {
							VerifyIfMatched.valueList.add(valueName);
							GeneralVisitor.putIntoMap(node, refactorNode.getMap);
							dfa.state = State.StateA1;

						}
					}

				}
				return true;
			}
		});

	}

	public static void collectInvoke(ASTNode cuu,CommonRefactorNode refactorNode,ModeMatchDFA dfa) {

		cuu.accept(new ASTVisitor() {

			@Override
			public boolean visit(MethodInvocation invocation) {
				String[] types = invocation.resolveMethodBinding().getParameterNames();
				if (Arrays.asList(types).containsAll(VerifyIfMatched.valueList)) {
					// 所有模式都匹配上了
					GeneralVisitor.putIntoMap(invocation, refactorNode.invokeMap);
					// 最后一句invoke语句 可能不需要变化
					System.out.println("匹配的目标statement is:" + cuu);
					dfa.state = State.STATEA_ACCCPTED;
					
				}

				return true;

			}
		});
		
	
	}

	
	


	
	
	

}
