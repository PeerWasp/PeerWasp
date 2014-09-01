package org.peerbox.watchservice;

import static org.junit.Assert.*;

public class StatePatternTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileContext context = new FileContext();
		FileActionState myFile = new StartActionState(context);
		context.getCurrentState();
		myFile.handleCreateEvent();
		context.getCurrentState();
		
	   }

	}

