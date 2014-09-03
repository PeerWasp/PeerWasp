package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;

public class StatePatternTest {
	
		
	public static void main(String[] args) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation, IOException {
		// TODO Auto-generated method stub
		

		Action context = new Action();
		//context.createEvent();
		context.handleModifyEvent();
		context.getCurrentState();
		//context.deleteEvent();
		context.getCurrentState();
		
		context.execute();
		
	   }

	}

