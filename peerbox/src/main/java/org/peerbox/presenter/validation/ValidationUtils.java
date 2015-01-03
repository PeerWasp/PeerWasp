package org.peerbox.presenter.validation;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.peerbox.app.Constants;
import org.peerbox.app.manager.UserManager;

public class ValidationUtils {
	
	public enum ValidationResult {
		OK("ok"),
		ERROR("error"),
		
		USERNAME_EMPTY("Username cannot be empty."),
		USERNAME_ALREADY_TAKEN("Username is already taken."),
		USER_NOT_EXISTS("Username does not exist."),
		
		PASSWORD_EMPTY("Password cannot be empty."),
		PASSWORD_TOO_SHORT(String.format(
				"The password should be at least %d characters long.", Constants.MIN_PASSWORD_LENGTH)),
		PASSWORD_MISMATCH("The passwords do not match."),
		
		PIN_EMPTY("Pin cannot be empty."),
		PIN_TOO_SHORT(String.format(
				"The pin should be at least %d characters long.", Constants.MIN_PIN_LENGTH)),
		PIN_MISMATCH("The pins do not match."),
		
		BOOTSTRAPHOST_EMPTY("The bootstrap host cannot be empty."),
		
		ROOTPATH_NOTEXISTS("Directory does not exist."),
		ROOTPATH_NOTADIRECTORY("Path is not a directory."),
		ROOTPATH_NOTWRITABLE("Directory is not writable."),
		ROOTPATH_CREATE_ACCESSDENIED("Could not create the directory (access denied)."),
		ROOTPATH_CREATE_FAILED("Could not create the directory.");
		
		
		
		private String message;

		private ValidationResult(String message) {
			this.message = message;
		}
		
		public boolean isError() {
			return this != OK;
		}

		public String getMessage() {
			return message;
		}
	}
	
	public static ValidationResult validateUsername(final String username, boolean checkIfRegistered, final UserManager userManager) throws NoPeerConnectionException {
		if(username == null) {
			throw new IllegalArgumentException("Argument username must not be null.");
		}
		if(checkIfRegistered && userManager == null) {
			throw new IllegalArgumentException("Argument userManager must not be null if checkIfRegistered is true");
		}
		
		if(username.trim().isEmpty()) {
			return ValidationResult.USERNAME_EMPTY;
		}
		
		if(checkIfRegistered && userManager != null) {
			if(userManager.isRegistered(username)) {
				return ValidationResult.USERNAME_ALREADY_TAKEN;
			}
		}
		
		return ValidationResult.OK;
	}
	
	public static ValidationResult validateUserExists(final String username, boolean checkIfRegistered, final UserManager userManager) throws NoPeerConnectionException {
		if(username == null) {
			throw new IllegalArgumentException("Argument username must not be null.");
		}
		if(checkIfRegistered && userManager == null) {
			throw new IllegalArgumentException("Argument userManager must not be null if checkIfRegistered is true.");
		}
		
		if(username.trim().isEmpty()) {
			return ValidationResult.USERNAME_EMPTY;
		}
		
		if(checkIfRegistered && userManager != null) {
			if(!userManager.isRegistered(username)) {
				return ValidationResult.USER_NOT_EXISTS;
			}
		}
		
		return ValidationResult.OK;
	}
	
	public static ValidationResult validatePins(final String pin, final String confirmPin) {
		if(pin == null) {
			throw new IllegalArgumentException("Argument pin must not be null.");
		}
		if(confirmPin == null) {
			throw new IllegalArgumentException("Argument confirmPin must not be null.");
		}
		
		if (pin.isEmpty()) {
			return ValidationResult.PIN_EMPTY;
		}

		if (pin.length() < Constants.MIN_PIN_LENGTH) {
			return ValidationResult.PIN_TOO_SHORT;
		}
		
		if(!pin.equals(confirmPin)) {
			return ValidationResult.PIN_MISMATCH;
		}
		
		if(confirmPin.isEmpty()) {
			return ValidationResult.PIN_EMPTY;
		}
		
		return ValidationResult.OK;
	}

	public static ValidationResult validatePasswords(final String password, final String confirmPassword) {
		if(password == null) {
			throw new IllegalArgumentException("Argument password must not be null.");
		}
		if(confirmPassword == null) {
			throw new IllegalArgumentException("Argument confirmPassword must not be null.");
		}
		
		if (password.isEmpty()) {
			return ValidationResult.PASSWORD_EMPTY;
		}

		if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
			return ValidationResult.PASSWORD_TOO_SHORT;
		}
		
		if(!password.equals(confirmPassword)) {
			return ValidationResult.PASSWORD_MISMATCH;
		}
		
		if(confirmPassword.isEmpty()) {
			return ValidationResult.PASSWORD_EMPTY;
		}
		
		return ValidationResult.OK;
	}
}

