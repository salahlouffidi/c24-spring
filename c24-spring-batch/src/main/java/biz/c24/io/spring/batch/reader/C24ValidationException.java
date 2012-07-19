package biz.c24.io.spring.batch.reader;

import org.springframework.batch.item.validator.ValidationException;

import biz.c24.io.api.data.ComplexDataObject;

public class C24ValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	private ComplexDataObject cdo;
	
	public C24ValidationException(String message, ComplexDataObject cdo, Throwable cause) {
		super(message, cause);
		this.cdo = cdo;
	}
	
	public ComplexDataObject getCdo() {
		return cdo;
	}

}
