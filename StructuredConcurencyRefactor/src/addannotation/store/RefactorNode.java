package addannotation.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class RefactorNode {
	
	public LinkedHashMap<Integer,VariableDeclarationStatement> executorMap;
	public LinkedHashMap<Integer,VariableDeclarationStatement> futureMap;
	public LinkedHashMap<Integer,VariableDeclarationStatement> getMap;
	public LinkedHashMap<Integer,Statement> invokeMap;
	public MethodDeclaration methodDeclaration;
	public List<Statement> stmtbeSubAndGet;
	public List<Statement> stmtbeGetAndInvoke;
	
	
	public RefactorNode(CompilationUnit root,MethodDeclaration methodDeclaration) {
		this.executorMap = new LinkedHashMap<>();
		this.futureMap = new LinkedHashMap<>();
		this.getMap = new LinkedHashMap<>();
		this.invokeMap = new LinkedHashMap<>();
		this.root = root;
		this.methodDeclaration = methodDeclaration;
		this.stmtbeSubAndGet = new ArrayList<>();
		this.stmtbeGetAndInvoke = new ArrayList<>();
	}
	
	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}


	public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}


	private CompilationUnit root;
	
	
	public CompilationUnit getRoot() {
		return root;
	}


	public void setRoot(CompilationUnit root) {
		this.root = root;
	}


	
	/* 
	 * 如果匹配失败的话，就是删除所有存储的node
	 *  */
	public void delete() {
		futureMap.clear();
		getMap.clear();
		invokeMap.clear();
		executorMap.clear();
		stmtbeSubAndGet.clear();
		stmtbeGetAndInvoke.clear();
	
	}

	@Override
	public String toString() {
		return "RefactorNode [executorMap=" + executorMap + ", futureMap=" + futureMap + ", getMap=" + getMap
				+ ", invokeMap=" + invokeMap + ", methodDeclaration=" + methodDeclaration + ", stmtbeSubAndGet="
				+ stmtbeSubAndGet + ", stmtbeGetAndInvoke=" + stmtbeGetAndInvoke + ", root=" + root + "]";
	}



	
	
	
	
	
	
	
	
	
	
	

}
