package addannotation.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

@SuppressWarnings("unused")
public class InvokeAllRefactorNode   {
	
	public LinkedHashMap<Integer, Statement> executorMap;
	public List invokeAllStmt;
	public List tryStmt;
	public List forStmt;
	public MethodInvocation getMethodInvocation;
	
	public MethodDeclaration methodDeclaration;
	
	
	
	public InvokeAllRefactorNode(MethodDeclaration methodDeclaration) {
	
		this.executorMap = new LinkedHashMap<>();
		this.invokeAllStmt = new ArrayList<>();
		this.tryStmt = new ArrayList<>();
		this.forStmt = new ArrayList<>();
		this.methodDeclaration = methodDeclaration;
		
	}
	
	
	
	public void delete() {
		invokeAllStmt.clear();
		tryStmt.clear();
		forStmt.clear();
		executorMap.clear();
	
	}



	@Override
	public String toString() {
		return "InvokeAllRefactorNode [executorMap=" + executorMap + ", invokeAllStmt=" + invokeAllStmt + ", tryStmt="
				+ tryStmt + ", forStmt=" + forStmt + ", getMethodInvocation=" + getMethodInvocation
				+ ", methodDeclaration=" + methodDeclaration + "]";
	}



	
	
	
	
	
	


	
	
	
}
