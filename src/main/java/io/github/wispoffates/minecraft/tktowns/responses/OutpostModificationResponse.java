package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.Outpost;

public class OutpostModificationResponse {
	protected String message;
	protected Outpost outpost;
	protected boolean success;
	
	public OutpostModificationResponse(String message, Outpost outpost, boolean success) {
		this.message = message;
		this.outpost = outpost;
		this.success = success;
	}
	
	public String getMessage() {
		return message;
	}

	public Outpost getOutpost() {
		return outpost;
	}

	public boolean isSuccess() {
		return success;
	}
}
