package addannotation.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
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
import addannotation.store.InvokeAllRefactorNode;
import addannotation.store.VerifyIfMatched;

public class GeneralVisitor {
	
	public static List executorList = new ArrayList<>();
	// 找到定义线程池的地方
		public static void collectExecutors(MethodDeclaration cuu,ModeMatchDFA dfa) {
			// TODO Auto-generated method stub
			cuu.accept(new ASTVisitor() {

				@Override
				public boolean visit(VariableDeclarationStatement node) {
					// TODO Auto-generated method stub
					String executorService = node.getType().toString();
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
					if (fragment.getInitializer() instanceof MethodInvocation) {
						MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
						String executors = invocation.getExpression().toString();
						String poolName = invocation.getName().getIdentifier();
						System.out.println("ExecutorService is :" + executorService);
						System.out.println("executors is :" + executors);
						System.out.println("poolName is:" + poolName.substring(0, 3));
						if (executorService.equals("ExecutorService") && executors.equals("Executors")
								&& poolName.substring(0, 3).equals("new")) {
							String poolNameRef = fragment.getName().getIdentifier();
							VerifyIfMatched.poolList.add(poolNameRef);
							System.out.println("poolNameRef is :" + poolNameRef);
							// 加入到refactorNode中去
							Block block = (Block) node.getParent();
							Integer index = block.statements().indexOf(node);
							executorList.add(index);
							executorList.add(node);
							dfa.state = State.INITIAL;

						}

					}

					return true;
				}

			});

		}
	
	// 收集一个java文件中所有的类
		public static void getTypeDeclaration(ASTNode cuu, List<TypeDeclaration> types) {
			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(TypeDeclaration node) {
					// TODO Auto-generated method stub
					types.add(node);
					return true;
				}
			});

		}

		public static void getMethodDeclaration(ASTNode cuu, List<MethodDeclaration> list) {
			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {
					list.add(node);
					return true;
				}
			});

		}
		
		
		public static void getTryStatement(ASTNode cuu, List<TryStatement> list) {
			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(TryStatement node) {
					list.add(node);
					return true;
				}
			});

		}
		
		
		
		public static List hasBlockParent(ASTNode node) {
			ASTNode parent = node.getParent();
			Map<Integer, Statement> map = new HashMap<>();
			List list = new ArrayList<>();
			while (parent != null) {
				if (parent instanceof Block && node instanceof Statement) {
					
					Block block = (Block) parent;
					int index = block.statements().indexOf(node);
					
					list.add(index);
					list.add(node);
					map.put(index, (Statement) node);
					return list;
				}
				node = parent;
				parent = parent.getParent();
			}
			return null;
		}
		
		public static void putIntoMap(ASTNode node,Map<Integer,Statement> map) {
			List list = hasBlockParent(node);
			if(list != null) {
				Integer index = (Integer)list.get(0);
				Statement  statement = (Statement) list.get(1);
				
				map.put(index,statement);
				
			}
		
		}
		
		public static List putIntoList(ASTNode node,List resultList) {
			List list = hasBlockParent(node);
			if(list != null) {
				Integer index = (Integer)list.get(0);
				Statement  statement = (Statement) list.get(1);
				
				resultList.add(index);
				resultList.add(statement);
				
			}
			return resultList;
			
			
		}
		
		public static void putIntoExecutorMap(Map<Integer,Statement> map ) {
				System.out.println("executorList is:" + executorList);
				if(!executorList.isEmpty()) {
					Integer index = (Integer)executorList.get(0);
					Statement statement = (Statement)executorList.get(1);
					map.put(index,statement);
					executorList.clear();
				}
				
				

			
		}

}
