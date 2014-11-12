package org.peerbox.presenter.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public final class EmptyTextFieldValidator {
	
	private TextField txt;
	private boolean trim;
	private ValidationResult returnOnError;
	private Decoration errorDecorator;

	public EmptyTextFieldValidator(TextField txt, boolean trim, ValidationResult returnOnError) {
		this.txt = txt;
		this.trim = trim;
		this.returnOnError = returnOnError;
		this.errorDecorator = new StyleClassDecoration("validation-error");
		initChangeListener();
	}

	private void initChangeListener() {
		txt.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue) {
				validate(newValue);
			}
		});
	}
	
	public ValidationResult validate() {
		return validate(txt.getText());
	}

	private ValidationResult validate(String newValue) {
		final String value = trim ? newValue.trim() : newValue;
		ValidationResult res = ValidationResult.ERROR;
		if(value.isEmpty()) {
			Decorator.addDecoration(txt, errorDecorator);
			res = returnOnError;
		} else {
			Decorator.removeDecoration(txt, errorDecorator);
			res = ValidationResult.OK;
		}
		return res;
	}

	public Decoration getDecorator() {
		return errorDecorator;
	}
}
