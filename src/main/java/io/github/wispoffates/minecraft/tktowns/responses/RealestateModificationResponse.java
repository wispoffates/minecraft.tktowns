package io.github.wispoffates.minecraft.tktowns.responses;

import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate;

public class RealestateModificationResponse extends GenericModificationResponse{
	protected RealEstate realestate;
	
	public RealestateModificationResponse(String message, RealEstate realestate, boolean success) {
		super(message,success);
		this.realestate = realestate;
	}

	public RealEstate getRealestate() {
		return realestate;
	}

}
