package addannotation.DFA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.modifySource.CommonSourceRefactor;
import addannotation.modifySource.InvokeAllSourceRefactor;
import addannotation.store.CommonRefactorNode;
import addannotation.store.InvokeAllRefactorNode;
import addannotation.store.InvokeAnyRefactorNode;
import addannotation.store.VerifyIfMatched;
import addannotation.visitor.CommonVisitor;
import addannotation.visitor.GeneralVisitor;
import addannotation.visitor.InvokeAllVisitor;


@SuppressWarnings("unused")
public class ModeMatchDFA {
	
	
	public State state;
	public CommonRefactorNode commonRefactorNode;
	public InvokeAllRefactorNode invokeAllRefactorNode;
	public InvokeAnyRefactorNode invokeAnyRefactorNode;
	public MethodDeclaration methodDeclaration;
	public static Integer MaxStep = 4; 
	
	
	public ModeMatchDFA(MethodDeclaration methodDeclaration) {
		
		this.state = State.START;
		this.commonRefactorNode = new CommonRefactorNode(methodDeclaration);
		this.invokeAllRefactorNode = new InvokeAllRefactorNode(methodDeclaration);
		this.invokeAnyRefactorNode = new InvokeAnyRefactorNode();
		this.methodDeclaration = methodDeclaration;
		
		
	}
	
	public State modeMatch() {
		
		for(int i = 0;i <= MaxStep;i++) {
			switch (state) {
				case START:
					//先要找到定义线程池的语句
					GeneralVisitor.collectExecutors(methodDeclaration,this);
					break;
					
				case INITIAL:
					CommonVisitor.collectFutures(methodDeclaration,commonRefactorNode,this);
					InvokeAllVisitor.findInvokeAll(methodDeclaration, invokeAllRefactorNode, this);
				
					break;			
				case StateA:
					CommonVisitor.collectGetMethod(methodDeclaration,commonRefactorNode, this);
					break;
				case StateA1:
					CommonVisitor.collectInvoke(methodDeclaration, commonRefactorNode,this);
					break;
				case STATEA_ACCCPTED:
					CommonVisitor.collectBetweenSubmitAndGet(methodDeclaration, commonRefactorNode);
					CommonVisitor.collectBetweenGetAndInvoke(methodDeclaration, commonRefactorNode);
					return state;
					
				case STATEB_ACCCPTED:
					System.out.println("接受状态B");
					return state;
	
				default:
					break;			
			}
			
		}
		if(!state.equals(State.STATEA_ACCCPTED) || !state.equals(State.STATEB_ACCCPTED)) {
			commonRefactorNode.delete();
			invokeAllRefactorNode.delete();
			
		}
				
		
		return state;
		
			
	}
	
	public void refactorbyMode(State state) {
		
		switch (state) {
			case STATEA_ACCCPTED:
				CommonSourceRefactor CommonRefactor = new CommonSourceRefactor(commonRefactorNode);
				CommonRefactor.startRefactor();;
				break;
				
			case STATEB_ACCCPTED:
				InvokeAllSourceRefactor InvokeAllRefactor = new InvokeAllSourceRefactor(invokeAllRefactorNode);
				InvokeAllRefactor.startRefactor();
				break;
	
			default:
				System.out.println("nothing to refactor");
				break;
			}
	}

}
