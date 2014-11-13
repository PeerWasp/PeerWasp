package org.peerbox.presenter.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public final class EmptyTextFieldValidator extends TextFieldValidator {
	
	private boolean trim;
	private ValidationResult returnOnError;

	public EmptyTextFieldValidator(TextField txt, boolean trim, ValidationResult returnOnError) {
		super(txt, null);
		this.trim = trim;
		this.returnOnError = returnOnError;
		initChangeListener();
	}

	private void initChangeListener() {
		validateTxtField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue) {
				validate(newValue);
			}
		});
	}
	
	public ValidationResult validate() {
		return validate(validateTxtField.getText());
	}

	public ValidationResult validate(final String newValue) {
		final String value = trim ? newValue.trim() : newValue;
		ValidationResult res = ValidationResult.ERROR;
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
