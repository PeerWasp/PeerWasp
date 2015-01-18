package org.peerbox.events;

class AbstractGeneralMessage implements IGeneralMessage {

	private String title;
	private String description;

	protected AbstractGeneralMessage(final String title, final String description) {
		this.title = title;
		this.description = description;

		if (this.title == null) {
			this.title = "";
		}

		if (this.description == null) {
			this.description = "";
		}
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
