package org.peerbox.presenter.validation;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;

public class RootPathValidator extends TextFieldValidator {

	public RootPathValidator(TextField txtField, StringProperty errorProperty) {
		super(txtField, errorProperty, false);
	}
	
	@Override
	public ValidationResult validate(final String value) {
		Path path = Paths.get(value);
		ValidationResult res = SelectRootPathUtils.validateRootPath(path);
		if (res.isError()) {
			decorateError();
			setErrorMessage(res.getMessage());
		} else {
			undecorateError();
			clearErrorMessage();
		}
		return res;
	}

}
