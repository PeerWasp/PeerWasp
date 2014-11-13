package org.peerbox.presenter.validation;

import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;

import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public final class CombinedPasswordValidator {
	
	private TextFieldValidator validatePassword;
	private TextFieldValidator validateConfirmPassword;

	public CombinedPasswordValidator(PasswordField txtPassword,
			StringProperty passwordErrorProperty, PasswordField txtConfirmPassword) {
		validatePassword = new TextFieldValidator(txtPassword, passwordErrorProperty){
			@Override
			public ValidationResult validate(String password) {
				final String confirmPassword = validateConfirmPassword.getTextField().getText();
				return validatePasswords(password, confirmPassword);
			}
		};
		
		validateConfirmPassword = new TextFieldValidator(txtConfirmPassword){
			@Override
			public ValidationResult validate(String confirmPassword) {
				final String password = validatePassword.getTextField().getText();
				return validatePasswords(password, confirmPassword);
			}
		};
	}
	
	public ValidationResult validatePasswords() {
		final String password = validatePassword.getTextField().getText();
		final String confirmPassword = validateConfirmPassword.getTextField().getText();
		return validatePasswords(password, confirmPassword);
	}

	public ValidationResult validatePasswords(final String password, final String confirmPassword) {
		ValidationResult res = ValidationUtils.validatePasswords(password, confirmPassword);
		if (res.isError()) {
			validatePassword.decorateError();
			validateConfirmPassword.decorateError();
			validatePassword.setErrorMessage(res.getMessage());
		} else {
			validatePassword.undecorateError();
			validateConfirmPassword.undecorateError();
			validatePassword.clearErrorMessage();
		}
		return res;
	}

	public void reset() {
		validatePassword.undecorateError();
		validateConfirmPassword.undecorateError();
		validatePassword.clearErrorMessage();
		validateConfirmPassword.clearErrorMessage();
	}

}
