//package addannotation.utils;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
//import org.eclipse.jdt.core.dom.Block;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.IMethodBinding;
//import org.eclipse.jdt.core.dom.ITypeBinding;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.MethodInvocation;
//import org.eclipse.jdt.core.dom.Statement;
//import org.eclipse.jdt.core.dom.TryStatement;
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
//import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
//import addannotation.store.RefactorNode;
//import addannotation.store.VerifyIfMatched;
//
//@SuppressWarnings("unused")
//public class Visitor1 {
//	public static List<RefactorNode> refactorNodes = new ArrayList<>();
//	// 设置一个匹配成功的标记
//	public static boolean isMatched = false;
//
//	public static void collectTargetNode(List<Statement> statement, CompilationUnit root) {
//
//		
//		RefactorNode refactorNode = new RefactorNode(root, cuu);
//
//		Visitor.collectExecutors(cuu, verifyIfMatched, refactorNode);
//		System.out.println("poolList is :" + verifyIfMatched.poolList);
//		Visitor.collectFutures(cuu, verifyIfMatched, refactorNode);
//		// System.out.println("refactorNode futureMap is: " + refactorNode.futureMap);
//		Visitor.collectGetMethod(cuu, verifyIfMatched, refactorNode);
//		// System.out.println("refactorNode getMap is:" + refactorNode.getMap);
//
//		Visitor.collectBetweenSubmitAndGet(cuu, refactorNode);
//		// System.out.println("refactorNode submiAndget is:" + refactorNode.stmtbeSubAndGet);
//		Visitor.collectCombineResult(cuu, verifyIfMatched, refactorNode);
//		Visitor.collectBetweenGetAndInvoke(cuu, refactorNode);
//
//		System.out.println("refactorNode getAndInvoke is:" + refactorNode.stmtbeGetAndInvoke);
//
//		if (!isMatched) {
//			refactorNode.delete();
//			System.out.println("模式匹配失败，删除原先存储的节点信息");
//		} else {
//			System.out.println("模式匹配成功");
//			System.out.println("final refactorNode is :" + refactorNode);
//			refactorNodes.add(refactorNode);
//			// 如果是true的话，每次判断之后需要将该标志设置为false
//			isMatched = false;
//		}
//
//	}
//
//	// 找到定义线程池的地方
//	private static void collectExecutors(MethodDeclaration cuu, VerifyIfMatched verifyIfMatched,
//			RefactorNode refactorNode) {
//		// TODO Auto-generated method stub
//		cuu.accept(new ASTVisitor() {
//
//			@Override
//			public boolean visit(VariableDeclarationStatement node) {
//				// TODO Auto-generated method stub
//				String executorService = node.getType().toString();
//				VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
//				if (fragment.getInitializer() instanceof MethodInvocation) {
//					MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
//					String executors = invocation.getExpression().toString();
//					String poolName = invocation.getName().getIdentifier();
//					System.out.println("ExecutorService is :" + executorService);
//					System.out.println("executors is :" + executors);
//					System.out.println("poolName is:" + poolName.substring(0, 2));
//					System.out.println("poolName1 is:" + poolName.substring(0, 3));
//					if (executorService.equals("ExecutorService") && executors.equals("Executors")
//							&& poolName.substring(0, 3).equals("new")) {
//						String poolNameRef = fragment.getName().getIdentifier();
//						verifyIfMatched.poolList.add(poolNameRef);
//						System.out.println("poolNameRef is :" + poolNameRef);
//						// 加入到refactorNode中去
//						Block block = (Block) node.getParent();
//						Integer index = block.statements().indexOf(node);
//						refactorNode.executorMap.put(index, node);
//
//					}
//
//				}
//
//				return true;
//			}
//
//		});
//
//	}
//
//	public static void collectFutures(ASTNode cuu, VerifyIfMatched verifyIfMatched, RefactorNode refactorNode) {
//
//		cuu.accept(new ASTVisitor() {
//
//			@Override
//			public boolean visit(VariableDeclarationStatement node) {
//				// TODO Auto-generated method stub
//				VariableDeclarationStatement varStmt = (VariableDeclarationStatement) node;
//				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varStmt.fragments().get(0); // 存储所有的定义future的信息
//				String futureName = fragment.getName().getIdentifier();
//				System.out.println("vaiableName is : " + futureName);
//
//				if (fragment.getInitializer() instanceof MethodInvocation) {
//					MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
//					IMethodBinding methodBinding = invocation.resolveMethodBinding();
//					if (methodBinding != null) {
//
//						ITypeBinding type = methodBinding.getDeclaringClass();
//						String declareClass = type.getName();
//						String methodName = methodBinding.getName();
//						String retureType = methodBinding.getReturnType().getName();
//
//						if (declareClass.equals("ExecutorService") && methodName.equals("submit")
//								&& retureType.contains("Future")) {
//
//							System.out.println("declareClass is:" + declareClass);
//							System.out.println("methodName is:" + methodName);
//							System.out.println("retureType is:" + retureType);
//					
//							Map<Integer,Statement>  map = hasBlockParent(node);
//							
//							for(Integer key : map.keySet()) {
//								// 暂时先加入
//								refactorNode.futureMap.put(key, (VariableDeclarationStatement) map.get(key));
//							}
//						
//							verifyIfMatched.futureList.add(futureName);
//	
//						}
//					}
//
//				}
//				return true;
//			}
//
//		});
//
//	}
//	
//	
//	public static void collectBetweenSubmitAndGet(MethodDeclaration cuu, RefactorNode refactorNode) {
//		List<Statement> statements = cuu.getBody().statements();
//		Integer maxKey = MinOrMax.getMaxKey(refactorNode.futureMap);
//		System.out.println("maxkey is :" + maxKey);
//		Integer minkey = MinOrMax.getMinKey(refactorNode.getMap);
//
//		System.out.println("minKey is:" + minkey);
//
//		for (int i = maxKey + 1; i < minkey; i++) {
//			refactorNode.stmtbeSubAndGet.add(statements.get(i));
//
//		}
//	}
//
//	public static void collectBetweenGetAndInvoke(MethodDeclaration cuu, RefactorNode refactorNode) {
//		List<Statement> statements = cuu.getBody().statements();
//		Integer maxKey = MinOrMax.getMaxKey(refactorNode.getMap);
//		System.out.println("maxkey is :" + maxKey);
//		Integer minkey = MinOrMax.getMinKey(refactorNode.invokeMap);
//
//		System.out.println("minKey is:" + minkey);
//
//		for (int i = maxKey + 1; i < minkey; i++) {
//			refactorNode.stmtbeGetAndInvoke.add(statements.get(i));
//
//		}
//	}
//
//	public static void collectGetMethod(MethodDeclaration cuu, VerifyIfMatched verifyIfMatched,
//			RefactorNode refactorNode) {
//
//		cuu.accept(new ASTVisitor() {
//
//			@Override
//			public boolean visit(VariableDeclarationStatement node) {
//				// TODO Auto-generated method stub
//				VariableDeclarationStatement varStmt = (VariableDeclarationStatement) node;
//
//				VariableDeclarationFragment fragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
//				if (fragment != null) {
//					// 获取到如String str = future1.get() 中的str
//					String valueName = fragment.getName().toString();
//					if (fragment.getInitializer() instanceof MethodInvocation) {
//						MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
//						String getInvoker = invocation.getExpression().toString();
//						String methodInvoke = invocation.getName().toString();
//
//						if (methodInvoke.equals("get") && verifyIfMatched.futureList.contains(getInvoker)) {
//							verifyIfMatched.valueList.add(valueName);
//
//							// 这个地方法需要修改
//							Map<Integer,Statement>  map = hasBlockParent(node);
//							
//							for(Integer key : map.keySet()) {
//								// 暂时先加入
//								refactorNode.getMap.put(key, (VariableDeclarationStatement) map.get(key));
//							}
//						
//
//						}
//					}
//
//				}
//				return true;
//			}
//		});
//
//	}
//
//	public static void collectCombineResult(ASTNode cuu, VerifyIfMatched verifyIfMatched, RefactorNode refactorNode) {
//
//		cuu.accept(new ASTVisitor() {
//
//			@Override
//			public boolean visit(MethodInvocation invocation) {
//				String[] types = invocation.resolveMethodBinding().getParameterNames();
//				if (Arrays.asList(types).containsAll(verifyIfMatched.valueList)) {
//					// 所有模式都匹配上了
//					Map<Integer, Statement> map = hasBlockParent(invocation);
//					for (Integer key : map.keySet()) {
//						Statement statement = map.get(key);
//						refactorNode.invokeMap.put(key, statement);
//					}
//					// 最后一句invoke语句 可能不需要变化
//					isMatched = true;
//					System.out.println("匹配的目标statement is:" + cuu);
//
//				}
//
//				return true;
//
//			}
//		});
//	}
//
//	public static Map<Integer, Statement> hasBlockParent(ASTNode node) {
//		ASTNode parent = node.getParent();
//		Map<Integer, Statement> map = new HashMap<>();
//		while (parent != null) {
//			if (parent instanceof Block && node instanceof Statement) {
//				System.out.println("目标statement is:" + node);
//				Block block = (Block) parent;
//				int index = block.statements().indexOf(node);
//				System.out.println("目标代码的位置为：" + index);
//				map.put(index, (Statement) node);
//				return map;
//			}
//			node = parent;
//			parent = parent.getParent();
//		}
//		return null;
//	}
//	
//
//
//	// 收集一个java文件中所有的类
//	public static void getTypeDeclaration(ASTNode cuu, List<TypeDeclaration> types) {
//		cuu.accept(new ASTVisitor() {
//			@Override
//			public boolean visit(TypeDeclaration node) {
//				// TODO Auto-generated method stub
//				types.add(node);
//				return true;
//			}
//		});
//
//	}
//
//	public static void getMethodDeclaration(ASTNode cuu, List<MethodDeclaration> list) {
//		cuu.accept(new ASTVisitor() {
//			@Override
//			public boolean visit(MethodDeclaration node) {
//				list.add(node);
//				return true;
//			}
//		});
//
//	}
//}
