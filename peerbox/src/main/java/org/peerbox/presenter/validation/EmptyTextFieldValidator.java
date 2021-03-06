package org.peerbox.presenter.validation;

import javafx.scene.control.TextField;

import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public final class EmptyTextFieldValidator extends TextFieldValidator {
	
	private boolean trim;
	private ValidationResult returnOnError;

	public EmptyTextFieldValidator(TextField txt, boolean trim, ValidationResult returnOnError) {
		super(txt, null, true);
		this.trim = trim;
		this.returnOnError = returnOnError;
	}
	
	@Override
	public ValidationResult validate(final String newValue) {
		ValidationResult res = ValidationResult.ERROR;
		
		if(newValue == null) {
			return res;
		}
		
		final String value = trim ? newValue.trim() : newValue;
		if(value.isEmpty()) {
			decorateError();
			res = returnOnError;
			setErrorMessage(res.getMessage());
		} else {
			undecorateError();
			res = ValidationResult.OK;
			clearErrorMessage();
		}
		return res;
	}

	public void reset() {
		undecorateError();
		clearErrorMessage();
	}
}
