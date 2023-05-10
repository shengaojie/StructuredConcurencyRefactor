package addannotation.refactor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import addannotation.store.RefactorNode;
import addannotation.utils.MinOrMax;



@SuppressWarnings("unused")
public class SourceRefactor {

	public CompilationUnit root;
	public TypeDeclaration type;
	public RefactorNode node;
	public LinkedHashMap<Integer,VariableDeclarationStatement> executorMap;
	public LinkedHashMap<Integer,VariableDeclarationStatement> futureMap;
	public LinkedHashMap<Integer,VariableDeclarationStatement> getMap;
	public LinkedHashMap<Integer,Statement> invokeMap;
	public List<Statement> stmtbeSubAndGet;
	public List<Statement> stmtbeGetAndInvoke;

	public SourceRefactor(CompilationUnit root,RefactorNode node) {
		
		this.root = root;
		this.node = node;
		this.executorMap = node.executorMap;
		this.futureMap = node.futureMap;
		this.getMap = node.getMap;
		this.invokeMap = node.invokeMap;
		this.stmtbeSubAndGet = node.stmtbeSubAndGet;
		this.stmtbeGetAndInvoke = node.stmtbeGetAndInvoke;
	
	}

	
	public void refactorSource() {

	}

	// 实际修改一个java文件的中import信息
	@SuppressWarnings(value = { "unchecked" })
	public void importRefactor() {
		
	    List<ImportDeclaration> importList = root.imports();
	    AST ast = root.getAST();
		
		String [] content = {"jdk","incubator","concurrent","StructuredTaskScope"};	//以后需要导入的可能不止一个
		List<String []> contents = new ArrayList<>();
		contents.add(content);
		
		for (String[] con : contents) {
			ImportDeclaration newImport = ast.newImportDeclaration();
			newImport.setName(ast.newName(con));
			importList.add(newImport);
		}

	}

	@SuppressWarnings(value = { "unchecked" })
	public void tryRefactor() {
	
		AST ast = root.getAST();
		TryStatement tryResource = ast.newTryStatement();
		//创建try resource结构中的body
		Block tryBlock = ast.newBlock();
		//1.删除先关的线程池创建语句
		deleteExecutor();
		//2.给try 语句加上Resource结构
		addResource(tryResource,tryBlock,ast);
		//3.给try的body中加入scope.fork
		addFutures(tryBlock,ast);
		//4.给try的body中加入从最后一条scope.fork 到第一条future.resultNow语句之间的语句
		addStmtBetweenFutureAndGet(tryBlock,ast);
		//5.给try的body中加入所有的future.resultNow语句
		addGets(tryBlock,ast);
		//6.给try的body中加入从最后一条future.resultNow到最后联合调用的语句
		addStmeBetweenGetAndInvocation(tryBlock,ast);
		//7.给try的body中加入联合调用的语句
		addInvocation(tryBlock,ast);
		System.out.println("tryResource2 is :" + tryResource) ;
		
	}
	
	private void deleteExecutor() {
		
		//删除Executor相关的语句
		for (Integer key : executorMap.keySet()) {
			
		    executorMap.get(key).delete();
		  
		}
	}
	
	@SuppressWarnings(value = { "unchecked" })
	private void addResource(TryStatement tryResource,Block tryBlock,AST ast) {
	
		// 创建try 后面小括号中的内容
		// 设置variableDeclarationExpression左边的部分
		// 变量类型
		Type variableType = ast.newSimpleType(ast.newSimpleName("var"));
		
		// 变量名
		SimpleName scopeName = ast.newSimpleName("scope");
		
		// 创建VariableDeclarationFragment，由name和Initializer组成
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		//其中的Initializer是由ClassInstanceCreation组成的
		ClassInstanceCreation creation = ast.newClassInstanceCreation();
		//ClassInstanceCreation是由Type 和参数组成的 （这里的参数为空，所有没填）
		creation.setType(ast.newSimpleType(ast.newName("StructuredTaskScope.ShutdownOnFailure")));
		fragment.setInitializer(creation);
		fragment.setName(scopeName); // 设置变量名
		VariableDeclarationExpression varExpr1 = ast.newVariableDeclarationExpression(fragment);
		varExpr1.setType(variableType); // 设置变量类型
		
		System.out.println("varExpr1 ：" + varExpr1);
		tryResource.resources().add(varExpr1);
		tryResource.setBody(tryBlock);
		
		
		/*
		 * 获取到try语句的插入位置,在重构最开始的时候就要插入到MethodDeclaration， 
		 * 否则会因为别的语句的删除影响插入的位置
		 */
		Integer tryPosition = MinOrMax.getMinKey(futureMap);
		node.getMethodDeclaration().getBody().statements().add(tryPosition,tryResource);
		System.out.println("methodDeclaration is :" + node.getMethodDeclaration()) ;
	}
	
	
	@SuppressWarnings(value = { "unchecked" })
	private void addFutures(Block tryBlock,AST ast) {
		
		//先将所有的future加入到try的body种
		for (Integer key : futureMap.keySet()) {
		    VariableDeclarationStatement future = futureMap.get(key);

			VariableDeclarationFragment frag = (VariableDeclarationFragment)future.fragments().get(0);
			MethodInvocation invocation = (MethodInvocation)frag.getInitializer();
			invocation.setName(ast.newSimpleName("fork"));
			invocation.setExpression(ast.newSimpleName("scope"));
			VariableDeclarationStatement newFuture = (VariableDeclarationStatement) ASTNode.copySubtree(ast, future);
			tryBlock.statements().add(newFuture);
			future.delete();			
		  
		}
		
	}
	
	
	@SuppressWarnings(value = { "unchecked" })
	private void addStmtBetweenFutureAndGet(Block tryBlock,AST ast) {
		//将future 和 get方法之间的语句加入到try的body中
		for(Statement statement : stmtbeSubAndGet) {
			
			Statement newStatement = (Statement) ASTNode.copySubtree(ast, statement);
			tryBlock.statements().add(newStatement);
			statement.delete();
				
		}
		
		//插入两条中间语句join方法和throwIffailed
		MethodInvocation joinInvocation = ast.newMethodInvocation();
		joinInvocation.setExpression(ast.newSimpleName("scope"));
		joinInvocation.setName(ast.newSimpleName("join"));
		tryBlock.statements().add(ast.newExpressionStatement(joinInvocation));
		MethodInvocation throwInvocation = ast.newMethodInvocation();
		throwInvocation.setExpression(ast.newSimpleName("scope"));
		throwInvocation.setName(ast.newSimpleName("throwIfFailed"));
		tryBlock.statements().add(ast.newExpressionStatement(throwInvocation));
		
	}
	
	@SuppressWarnings(value = { "unchecked" })
	private void addGets(Block tryBlock,AST ast) {
		//将get方法换成resultnow 加入到try的body种去
		for(Integer key : getMap.keySet()) {
			VariableDeclarationStatement getStatement = getMap.get(key);
			VariableDeclarationFragment frag = (VariableDeclarationFragment)getStatement.fragments().get(0);
			MethodInvocation invocation = (MethodInvocation)frag.getInitializer();
			invocation.setName(ast.newSimpleName("resultNow"));
			
			VariableDeclarationStatement newGetStatement = (VariableDeclarationStatement) ASTNode.copySubtree(ast, getStatement);
			
			System.out.println("get is :" + newGetStatement);
			tryBlock.statements().add(newGetStatement);
			getStatement.delete();
			
		}
		
	}
	
	@SuppressWarnings(value = { "unchecked" })
	private void addStmeBetweenGetAndInvocation(Block tryBlock,AST ast) {
		//将get方法和最后方法调用之间的语句加入进去
		for(Statement statement : stmtbeGetAndInvoke) {
			
			Statement newStatement = (Statement) ASTNode.copySubtree(ast, statement);
			tryBlock.statements().add(newStatement);
			statement.delete();
			
		}
		
	}
	
	@SuppressWarnings(value = { "unchecked" })
	private void addInvocation(Block tryBlock,AST ast) {
//		添加最后方法调用的节点
		for(Integer key : invokeMap.keySet()) {
			Statement statement = invokeMap.get(key);
			Statement newStatement = (Statement) ASTNode.copySubtree(ast, statement);
			tryBlock.statements().add(newStatement);
			statement.delete();		
			
		}
		
	}

}
