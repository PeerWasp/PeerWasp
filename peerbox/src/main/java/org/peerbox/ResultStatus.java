package org.peerbox;

public class ResultStatus {
	
	private enum STATUS_CODE { OK, ERROR };
	
	private STATUS_CODE status;
	private String errorMessage;
	
	private ResultStatus(STATUS_CODE status) {
		this(status, null);
	}
	
	private ResultStatus(STATUS_CODE status, String message) {
		this.status = status;
		this.errorMessage = message;
	}
	
	public static ResultStatus ok() { return new ResultStatus(STATUS_CODE.OK, null); }
	public static ResultStatus error(String message) { return new ResultStatus(STATUS_CODE.ERROR, message); }
	public boolean isOk() { return status == STATUS_CODE.OK; }
	public boolean isError() { return !isOk(); }
	public String getErrorMessage() { return errorMessage; }
}
