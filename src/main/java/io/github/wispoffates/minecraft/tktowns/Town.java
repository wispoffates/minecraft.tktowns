package io.github.wispoffates.minecraft.tktowns;

import java.util.Map;
import java.util.UUID;

public class Town {

	protected UUID owner;
	protected RealEstate townRealEstate;
	protected Map<String, RealEstate> children;
	protected Map<String, Outpost> outposts;

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public RealEstate getTownRealEstate() {
		return townRealEstate;
	}

	public void setTownRealEstate(RealEstate townRealEstate) {
		this.townRealEstate = townRealEstate;
	}

	protected Map<String, RealEstate> getChildren() {
		return children;
	}
	
	//TODO: Add better accessors here add,remove what not.
	public void addChild(RealEstate re) {
		this.children.put(re.getName(), re);
	}
	
	public RealEstate getChild(String name) {
		return this.children.get(name);
	}
	
	protected void setChildren(Map<String, RealEstate> children) {
		this.children = children;
	}

}
