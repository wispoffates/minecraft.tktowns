package io.github.wispoffates.minecraft.tktowns.responses;

public class GenericModificationResponse {
	protected String message;
	protected boolean success;
	
	public GenericModificationResponse(String message, boolean success) {
		super();
		this.message = message;
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSuccess() {
		return success;
	}
	
	
}
