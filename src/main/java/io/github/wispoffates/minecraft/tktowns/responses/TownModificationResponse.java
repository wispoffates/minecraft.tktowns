package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.Town;

public class TownModificationResponse {
	protected String message;
	protected Town town;
	protected boolean success;
	
	public TownModificationResponse(String message, Town town, boolean success) {
		super();
		this.message = message;
		this.town = town;
		this.success = success;
	}
	
	public String getMessage() {
		return message;
	}

	public Town getTown() {
		return town;
	}

	public boolean isSuccess() {
		return success;
	}
}
