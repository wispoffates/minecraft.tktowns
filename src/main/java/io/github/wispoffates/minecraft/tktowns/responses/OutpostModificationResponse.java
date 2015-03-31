package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.Outpost;

public class OutpostModificationResponse extends GenericModificationResponse {
	protected Outpost outpost;
	
	public OutpostModificationResponse(String message, Outpost outpost, boolean success) {
		super(message,success);
		this.outpost = outpost;
	}

	public Outpost getOutpost() {
		return outpost;
	}
}
