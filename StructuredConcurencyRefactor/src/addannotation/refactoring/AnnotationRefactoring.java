package addannotation.refactoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.type.DeclaredType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;
import org.eclipse.text.edits.TextEdit;
import addannotation.DFA.ModeMatchDFA;
import addannotation.DFA.State;
import addannotation.modifySource.CommonSourceRefactor;
import addannotation.modifySource.InvokeAllSourceRefactor;
import addannotation.store.CommonRefactorNode;
import addannotation.store.VerifyIfMatched;
import addannotation.visitor.CommonVisitor;
import addannotation.visitor.GeneralVisitor;


/**
 * 此类是重构的动作类 重构的预览也是通过此类完成
 */
@SuppressWarnings("unused")
public class AnnotationRefactoring extends Refactoring {
	// 所有的重构变化
	List<Change> changeManager = new ArrayList<Change>();
	// 所有需要修改的JavaElement
	List<IJavaElement> compilationUnits = new ArrayList<IJavaElement>();
	
	
	// @Test ' s parameter
	boolean needTimeout = false;
	String timeoutValue = "500";
	/**
	 * 重构的构造方法
	 * 
	 * @param element
	 */
	public AnnotationRefactoring(IJavaElement element) {
		
		findAllCompilationUnits(element.getJavaProject());

	}

	/**
	 * 重构的后置条件，用于检查用户输入参数后是否满足某个条件
	 */
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			collectChanges();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (changeManager.size() == 0)
			return RefactoringStatus.createFatalErrorStatus("No testing methods found!");
		else
			return RefactoringStatus.createInfoStatus("Final condition has been checked");
	}

	/**
	 * 重构初始条件，用于检查重构开始前应满足的条件
	 */
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.createInfoStatus("Initial Condition is OK!");
	}

	/**
	 * 重构的代码变化 如果代码变化多于一处，则通过CompositeChange来完成
	 */
	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		Change[] changes = new Change[changeManager.size()];
		System.out.println("changeManager size is :" + changeManager.size());
		System.arraycopy(changeManager.toArray(), 0, changes, 0, changeManager.size());
		CompositeChange change = new CompositeChange("Add @Test annotation", changes);
		return change;
	}
	
	/**
	 * This method must have a return value, otherwise the finish button is not
	 * available
	 */
	@Override
	public String getName() {
		return "hello world";
	}

	/**
	 * iterate the project to find in all IPackageFragment
	 * @param project
	 */
	private void findAllCompilationUnits(IJavaProject project) {
		try {
			for (IJavaElement element : project.getChildren()) { // IPackageFragmentRoot
				if (element.getElementName().equals("src")) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) element;
					for (IJavaElement ele : root.getChildren()) {
						if (ele instanceof IPackageFragment) {
							IPackageFragment fragment = (IPackageFragment) ele;
							for (ICompilationUnit unit : fragment.getCompilationUnits()) {
								compilationUnits.add(unit);
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * create all the changes
	 * @throws JavaModelException
	 * @throws IOException
	 */
	private void collectChanges() throws JavaModelException, IOException {
		
		for (IJavaElement element : compilationUnits) {

			ICompilationUnit cu = (ICompilationUnit) element;
			// 获取对应的.java文件对应的源码
			String source = cu.getSource();
			Document document = new Document(source);
			CompilationUnit astRoot = createParser(cu);
			
			findAndRefactor(cu,document,astRoot);
			
		}
		
	}
	
	
	// creation of DOM/AST from a ICompilationUnit
	private CompilationUnit createParser(ICompilationUnit cu) {
		
		ASTParser parser = ASTParser.newParser(AST.JLS18);
		parser.setSource(cu);
		parser.setResolveBindings(true); // 打开绑定
		parser.setEnvironment(null, null, null, true); // setEnvironment（classpath,sourcepath,encoding,true）
		parser.setUnitName("example.java"); // 参数随意
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		VerifyIfMatched.astRoot = astRoot;
		return astRoot;
	}
	
	private void findAndRefactor(ICompilationUnit cu,Document document,CompilationUnit astRoot) {
		ArrayList<TypeDeclaration> typeList = new ArrayList<>();
		GeneralVisitor.getTypeDeclaration(astRoot, typeList);
		astRoot.recordModifications();
		for(TypeDeclaration type : typeList) {
			//找到需要修改的目标代码
			ArrayList<MethodDeclaration> methods = new ArrayList<>();
			GeneralVisitor.getMethodDeclaration(type, methods);
			for (MethodDeclaration method : methods) {
				VerifyIfMatched.curDeclaration = method;
				
				ModeMatchDFA dfa = new ModeMatchDFA(method);
				State modeType = dfa.modeMatch();
				if(modeType.equals(State.STATEA_ACCCPTED) || modeType.equals(State.STATEB_ACCCPTED)) {
					dfa.refactorbyMode(modeType);
					recordChanges(cu,document,astRoot);
					
				}
				
				
			}
		}
	}
	
	
	private void recordChanges(ICompilationUnit cu,Document document,CompilationUnit astRoot) {
	
		TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));
		TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
		change.setEdit(edits);
		changeManager.add(change);		
		
		
	}






}
