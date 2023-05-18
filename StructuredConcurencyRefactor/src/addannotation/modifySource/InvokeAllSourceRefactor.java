package addannotation.modifySource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
import addannotation.store.CommonRefactorNode;
import addannotation.store.InvokeAllRefactorNode;
import addannotation.store.VerifyIfMatched;
import addannotation.utils.MinOrMax;
import addannotation.visitor.InvokeAllVisitor;

@SuppressWarnings("unused")
public class InvokeAllSourceRefactor extends SourceRefactor {

	public List tryStmt;
	public List forStmt;
	public List invokeAllStmt;
	MethodInvocation getMethodInvocation;
	InvokeAllRefactorNode refactorNode;

	public InvokeAllSourceRefactor(InvokeAllRefactorNode refactorNode) {
		super(VerifyIfMatched.astRoot, refactorNode.executorMap, refactorNode.methodDeclaration);
		this.refactorNode = refactorNode;
		this.tryStmt = refactorNode.tryStmt;
		this.invokeAllStmt = refactorNode.invokeAllStmt;
		this.getMethodInvocation = refactorNode.getMethodInvocation;
		this.forStmt = refactorNode.forStmt;

	}

	@Override
	public void tryRefactor() {
		// TODO Auto-generated method stub
		AST ast = root.getAST();
		Integer invokeAllIndex = (Integer) invokeAllStmt.get(0);
		VariableDeclarationStatement invokeAllStatement = (VariableDeclarationStatement) invokeAllStmt.get(1);
		Integer futureListIndex = MinOrMax.getMaxKey(executorMap);
		System.out.println("futureListIndex is :" + futureListIndex);
		Integer tryStmtIndex = (Integer) tryStmt.get(0);
		TryStatement tryStatement = (TryStatement) tryStmt.get(1);
		Integer forStmtIndex = (Integer) forStmt.get(0);

		addResource(tryStatement, ast);
		addFutureCollection(ast, invokeAllStatement, futureListIndex);
		replaceInvokeAllwithForStmt(ast, tryStatement, invokeAllStatement, invokeAllIndex);
		addJoin(tryStatement, ast, forStmtIndex);
		modifygetToResultNow(ast);
		handleFinally(tryStatement);
		System.out.println("this methodDeclaration :" + methodDeclaration);
	}

	@SuppressWarnings(value = { "unchecked" })
	private void addResource(TryStatement tryStatement, AST ast) {
		// add resource
		// 设置variableDeclarationExpression左边的部分
		// 变量类型
		Type variableType = ast.newSimpleType(ast.newSimpleName("var"));

		// 变量名
		SimpleName scopeName = ast.newSimpleName("scope");
		// 创建VariableDeclarationFragment，由name和Initializer组成
		VariableDeclarationFragment fragment1 = ast.newVariableDeclarationFragment();
		// 其中的Initializer是由ClassInstanceCreation组成的
		ClassInstanceCreation creation1 = ast.newClassInstanceCreation();
		// ClassInstanceCreation是由Type 和参数组成的 （这里的参数为空，所有没填）
		creation1.setType(ast.newSimpleType(ast.newName("StructuredTaskScope.ShutdownOnFailure")));
		fragment1.setInitializer(creation1);
		fragment1.setName(scopeName); // 设置变量名
		VariableDeclarationExpression varExpr1 = ast.newVariableDeclarationExpression(fragment1);
		varExpr1.setType(variableType); // 设置变量类型
		tryStatement.resources().add(varExpr1);

	}

	private void addFutureCollection(AST ast, VariableDeclarationStatement invokeAllStatement, int index) {
		// 组装一个methodDeclaration

		String collectionName = invokeAllStatement.getType().toString();
		System.out.println("collectionName is" + collectionName);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) invokeAllStatement.fragments().get(0);
		String collectionRef = fragment.getName().toString();
		ClassInstanceCreation creation = ast.newClassInstanceCreation();
		if (collectionName.contains("List")) {

			creation.setType(ast.newSimpleType(ast.newName("ArrayList")));

		} else if (collectionName.contains("Set")) {
			creation.setType(ast.newSimpleType(ast.newName("HashSet")));

		} else {

		}

		fragment.setInitializer(creation);
		VariableDeclarationStatement newVarDecStmt = (VariableDeclarationStatement) ASTNode.copySubtree(ast,
				invokeAllStatement);
		System.out.println("newVarDecStmt is :" + newVarDecStmt);
		methodDeclaration.getBody().statements().add(index + 1, newVarDecStmt);
		invokeAllStatement.delete();
	}

	private void replaceInvokeAllwithForStmt(AST ast, TryStatement tryStatement,
			VariableDeclarationStatement invokeAllStatement, int index) {

		// 将原先的invokeAll语句换成for
		EnhancedForStatement forStatement = ast.newEnhancedForStatement();
		// 设置循环变量
		SingleVariableDeclaration loopVar = ast.newSingleVariableDeclaration();
		loopVar.setType(ast.newSimpleType(ast.newName(InvokeAllVisitor.arguementType))); // 设置变量类型为int
		loopVar.setName(ast.newSimpleName("task1")); // 设置变量名为i
		forStatement.setParameter(loopVar);

		// 设置迭代器
		forStatement.setExpression(ast.newSimpleName(InvokeAllVisitor.arguementName));

		// 设置循环体
		Block body = ast.newBlock();
		// 设置body中的语句

		MethodInvocation methodArguement = ast.newMethodInvocation();
		methodArguement.setExpression(ast.newSimpleName("scope"));
		methodArguement.setName(ast.newSimpleName("fork"));
		methodArguement.arguments().add(ast.newSimpleName("task1"));

		MethodInvocation invocation = ast.newMethodInvocation();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) invokeAllStatement.fragments().get(0);
		String collectionRef = fragment.getName().toString();
		invocation.setExpression(ast.newSimpleName(collectionRef));
		invocation.setName(ast.newSimpleName("add"));
		invocation.arguments().add(methodArguement);

		ExpressionStatement newStatement = ast.newExpressionStatement(invocation);
		body.statements().add(newStatement);
		System.out.println("body is :" + body);
		forStatement.setBody(body);
		tryStatement.getBody().statements().add(index, forStatement);

	}

	public void addJoin(TryStatement tryStatement, AST ast, int index) {
		// 增加一个join方法

		MethodInvocation joinInvocation = ast.newMethodInvocation();
		joinInvocation.setExpression(ast.newSimpleName("scope"));
		joinInvocation.setName(ast.newSimpleName("join"));
		tryStatement.getBody().statements().add(index, ast.newExpressionStatement(joinInvocation));

	}

	public void modifygetToResultNow(AST ast) {

		getMethodInvocation.setName(ast.newSimpleName("resultNow"));
	}

	public void handleFinally(TryStatement tryStatement) {
		// 如果finally中只有一句话，且是shutdown，可以删除掉整个finally
		// 如果有多个语句，只删除掉其中的shutdown语句
		List<Statement> statements = tryStatement.getFinally().statements();
		if (statements.size() == 1 && statements.get(0) instanceof ExpressionStatement) {

			ExpressionStatement expression = (ExpressionStatement) statements.get(0);
			if (expression.getExpression() instanceof MethodInvocation) {
				MethodInvocation invocation = (MethodInvocation) expression.getExpression();
				String closeName = invocation.getName().toString();
				if (closeName.equals("shutdown")) {
					tryStatement.setFinally(null);

				}
			}
		} else {
			for (Statement statement : statements) {
				if (statement instanceof ExpressionStatement) {

					ExpressionStatement expression = (ExpressionStatement) statement;
					if (expression.getExpression() instanceof MethodInvocation) {
						MethodInvocation invocation = (MethodInvocation) expression.getExpression();
						String closeName = invocation.getName().toString();
						if (closeName.equals("shutdown")) {
							tryStatement.getFinally().statements().remove(statement);

						}
					}

				}

			}

		}

	}

}
