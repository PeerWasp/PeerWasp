package org.peerbox.watchservice;

import static org.junit.Assert.*;

public class StatePatternTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Action context = new Action();
		context.createEvent();
		context.getCurrentState();
		context.deleteEvent();
		context.getCurrentState();
		
		
	   }

	}

