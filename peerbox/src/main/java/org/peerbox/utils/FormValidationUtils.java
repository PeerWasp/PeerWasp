package org.peerbox.utils;

import javafx.scene.control.TextField;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationObject;
import jidefx.scene.control.validation.Validator;

public class FormValidationUtils {

	public static Validator createEmptyTextFieldValidator(TextField textField, String errorMsg, boolean trim) {
		return new Validator() {
			@Override
			public ValidationEvent call(ValidationObject param) {
				if(param.getSource() instanceof TextField) {
					String txt = ((TextField)param.getSource()).getText();
					if(trim) {
						txt = txt.trim();
					}
					if (txt.isEmpty()) {
						return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, errorMsg);
					}
					return ValidationEvent.OK;
				}
				return ValidationEvent.UNKNOWN;
			}

		};
	}
	
}
