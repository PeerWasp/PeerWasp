package org.peerbox.view.converter;

import javafx.util.StringConverter;

public class EnabledDisabledStringConverter extends StringConverter<Boolean> {
	@Override
	public String toString(Boolean bool) {
		return (bool ? "Enabled" : "Disabled");
	}

	@Override
	public Boolean fromString(String s) {
		return s.equalsIgnoreCase("enabled");
	}
}
