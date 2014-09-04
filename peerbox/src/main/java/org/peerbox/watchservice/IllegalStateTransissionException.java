package org.peerbox.watchservice;

public class IllegalStateTransissionException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3996963186832789582L;

	public IllegalStateTransissionException(){
		super();
	}
	
	public IllegalStateTransissionException(String message){
		super(message);
	}
}
