package org.ih.prescription.data.exchange.scheduler;

public class MpiNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	MpiNotFoundException(String msg){
		super(msg);
	}

}
