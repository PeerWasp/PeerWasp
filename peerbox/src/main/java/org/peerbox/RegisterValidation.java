package org.peerbox;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.model.H2HManager;
import org.peerbox.Constants;


public class RegisterValidation {

	public static boolean checkUsername(String username) throws NoPeerConnectionException {
		if(H2HManager.INSTANCE.checkIfRegistered(username)) {
			System.out.println("This user is already registered");
			return false;
		} else {
			System.out.println("This user is not registered so far.");
			return true;
		}
	}
	
	//checks whether the password is long enough according to the constant value
	public static boolean checkPasswordLength(String pwd_1){
		if (pwd_1.length() >= Constants.MIN_PASSWORD_LENGTH){
			System.out.println("Password length okay.");
			return true;
		} else {
			System.err.println("Password is too short. Needs at least " + Constants.MIN_PASSWORD_LENGTH + " characters.");
			return false;
		}
	}
	
	//checks whether the passwords entered in the register view matches each other
	public static boolean checkPasswordMatch(String pwd_1, String pwd_2){
		if(pwd_1.equals(pwd_2)){
			System.out.println("Passwords identical.");
			return true;
		} else {
			System.err.println("Passwords not identical. Try again");
			return false;
			}
	}
	
	
	//checks whether the pin is long enough according to the constant value
	public static boolean checkPinLength(String pin_1){		
		if (pin_1.length() >= Constants.MIN_PIN_LENGTH){
			System.out.println("PIN length okay.");
			return true;
		} else {
			System.err.println("PIN is too short. Needs at least " + Constants.MIN_PIN_LENGTH + " characters.");
			return false;
		}
		
	}
	
	public static boolean checkPinMatch(String pin_1, String pin_2){
		if(pin_1.equals(pin_2)){
			System.out.println("PINs identical.");
			return true;
		} else {
			System.err.println("PINs not identical. Try again");
			return false;
		}
	}
}
