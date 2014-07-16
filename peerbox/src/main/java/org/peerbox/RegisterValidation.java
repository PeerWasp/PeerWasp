package org.peerbox;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.controller.H2HManager;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterValidation {

	public static boolean checkUsername(String username) throws NoPeerConnectionException{
		//TODO
		
		if(H2HManager.INSTANCE.checkIfRegistered(username)) {
			System.out.println("This user is already registered");
			return false;
		} else {
			System.out.println("This user is not registered so far.");
			return true;
		}
	}
	
	public static boolean checkPassword(String pwd_1, String pwd_2){
		if (pwd_1.length() >= 6){
			System.out.println("Password length okay");
			
				if(pwd_1.equals(pwd_2)){
					System.out.println("Passwords identical.");
					return true;
			} else {
				System.err.println("Passwords not identical. Try again");
			}
		} else {
			System.err.println("Password too short. Needs at least 6 characters!");
		}
		
		return false;
	}
	
	public static boolean checkPIN(String pin_1, String pin_2){
		if (pin_1.length() >= 3){
			System.out.println("PIN length okay");
			
				if(pin_1.equals(pin_2)){
					System.out.println("PINs identical.");
					return true;
			} else {
				System.err.println("PINs not identical. Try again");
			}
		} else {
			System.err.println("PIN too short. Needs at least 3 characters!");
		}
		
		return false;
	}
}
