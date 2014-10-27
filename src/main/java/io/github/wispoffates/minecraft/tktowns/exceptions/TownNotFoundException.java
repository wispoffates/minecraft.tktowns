package io.github.wispoffates.minecraft.tktowns.exceptions;

public class TownNotFoundException extends TKTownsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TownNotFoundException() {
	}

	public TownNotFoundException(String arg0) {
		super(arg0);
	}

	public TownNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public TownNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TownNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
