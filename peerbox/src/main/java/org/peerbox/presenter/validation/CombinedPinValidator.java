package org.peerbox.presenter.validation;

import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;

import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public final class CombinedPinValidator {
	
	private TextFieldValidator validatePin;
	private TextFieldValidator validateConfirmPin;

	public CombinedPinValidator(PasswordField txtPin,
			StringProperty pinErrorProperty, PasswordField txtConfirmPin) {
		validatePin = new TextFieldValidator(txtPin, pinErrorProperty){
			@Override
			public ValidationResult validate(String pin) {
				final String confirmPin = validateConfirmPin.getTextField().getText();
				return validatePins(pin, confirmPin);
			}
		};
		
		validateConfirmPin = new TextFieldValidator(txtConfirmPin){
			@Override
			public ValidationResult validate(String confirmPin) {
				final String pin = validatePin.getTextField().getText();
				return validatePins(pin, confirmPin);
			}
		};
	}
	
	public ValidationResult validatePins() {
		final String pin = validatePin.getTextField().getText();
		final String confirmPin = validateConfirmPin.getTextField().getText();
		return validatePins(pin, confirmPin);
	}

	public ValidationResult validatePins(final String pin, final String confirmPin) {
		ValidationResult res = ValidationUtils.validatePins(pin, confirmPin);
		if (res.isError()) {
			validatePin.decorateError();
			validateConfirmPin.decorateError();
			validatePin.setErrorMessage(res.getMessage());
		} else {
			validatePin.undecorateError();
			validateConfirmPin.undecorateError();
			validatePin.clearErrorMessage();
		}
		return res;
	}
	
	public void reset() {
		validatePin.undecorateError();
		validateConfirmPin.undecorateError();
		validatePin.clearErrorMessage();
		validateConfirmPin.clearErrorMessage();
	}

}
