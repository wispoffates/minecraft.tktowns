package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate;

public class RealestateModificationResponse {
	protected String message;
	protected RealEstate realestate;
	protected boolean success;
	
	public RealestateModificationResponse(String message, RealEstate realestate, boolean success) {
		this.message = message;
		this.realestate = realestate;
		this.success = success;
	}
	
	public String getMessage() {
		return message;
	}

	public RealEstate getRealestate() {
		return realestate;
	}

	public boolean isSuccess() {
		return success;
	}
}
