package biz.c24.io.spring.batch;

import java.util.Collection;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationEvent;
import biz.c24.io.spring.batch.reader.C24ValidationException;

/**
 * Exception class used to hold multiple validation exceptions
 * As a result the 'cause' exception is null - to get full information on the causes of failure
 * use the getCause method.
 * 
 * @author Andrew Elmore
 *
 */
public class C24CompoundValidationException extends C24ValidationException {

	private static final long serialVersionUID = -5457226067917371972L;
	
	/**
	 * The validation failures
	 */
	private Collection<ValidationEvent> failures = null;
	
	/**
	 * Construct a C24CompoundValidationException
	 * @param cdo The ComplexDataObject which failed validation
	 * @param failures The set of events which caused validation to fail
	 */
	public C24CompoundValidationException(ComplexDataObject cdo, Collection<ValidationEvent> failures) {
		super("Mulitple validation failures", cdo, null);
		this.failures = failures;
	}
	
	/**
	 * Get the validation events which caused validation failure
	 * @return The Collection of failure ValidationEvents
	 */
	public Collection<ValidationEvent> getFailures() {
		return failures;
	}

}
