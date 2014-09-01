package org.peerbox.watchservice;

import static org.junit.Assert.*;

public class StattePatternTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileContext context = new FileContext();

	    //testing Add File state behavior  
		CreateFileAction createState = new CreateFileAction(context);
	    context.getCurrentState();
	    System.out.println("--------");
	    
	    createState.handleCreateEvent();
	    context.getCurrentState();
	    System.out.println("--------");
	    
	    createState.handleDeleteEvent();
	    context.getCurrentState();
	   }
	}

