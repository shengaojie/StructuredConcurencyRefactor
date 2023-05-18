package addannotation.store;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class VerifyIfMatched {
	
	public static ArrayList<String> futureList = new ArrayList<>();
	public static  ArrayList<String> valueList = new ArrayList<>();
	public static ArrayList<String> poolList = new ArrayList<>();
	public static MethodDeclaration curDeclaration;
	public static CompilationUnit astRoot;
	
//	public VerifyIfMatched(MethodDeclaration curDeclaration) {
//		
//		this.futureList = new ArrayList<>();
//		this.valueList = new ArrayList<>();
//		this.poolList = new ArrayList<>();
//		this.curDeclaration = curDeclaration;
//	}
//	
	
	
	
	public static void clearAll() {
		
		futureList.clear();
		valueList.clear();
		poolList.clear();
		
	}
	
	
	
	
	
	
	
}
