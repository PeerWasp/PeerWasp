package org.peerbox.watchservice;

import static org.junit.Assert.*;

public class StattePatternTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileContext context = new FileContext();

	    //testing Add File state behavior  
		AddFileAction addState = new AddFileAction();
	    

	    System.out.println(context.getState().toString());

	    //testing Delete File state behavior  
	    DeleteFileAction deleteState = new DeleteFileAction();
	    

	    System.out.println(context.getState().toString());
	      
	    //testing Modify File state behavior  
	    ModifyFileAction modifyState = new ModifyFileAction();
	   

	    System.out.println(context.getState().toString());
	    
	    //testing Move File state behavior  
	    MoveFileAction moveState = new MoveFileAction();

	    System.out.println(context.getState().toString());
	   }
	}

