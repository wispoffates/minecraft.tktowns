package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.Town;

public class TownModificationResponse extends GenericModificationResponse {
	protected Town town;
	
	public TownModificationResponse(String message, Town town, boolean success) {
		super(message,success);
		this.town = town;
	}

	public Town getTown() {
		return town;
	}
}
