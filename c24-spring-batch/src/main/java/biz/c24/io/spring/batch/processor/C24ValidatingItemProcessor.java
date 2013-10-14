/*
 * Copyright 2012 C24 Technologies.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.spring.batch.processor;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationEvent;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.data.ValidationListener;
import biz.c24.io.api.data.ValidationManager;
import biz.c24.io.spring.batch.C24CompoundValidationException;
import biz.c24.io.spring.batch.reader.C24ValidationException;

/**
 * An ItemProcessor that validates a ComplexDataObject.
 * While no changes are made to the ComplexDataObject, it does allow an ItemProcessorListener to be 
 * wired in to catch any ComplexDataObjects which fail validation.
 * 
 * @author AndrewElmore
 *
 */
public class C24ValidatingItemProcessor implements ItemProcessor<ComplexDataObject, ComplexDataObject>, ItemStream {
	
	private ThreadLocal<ValidationManager> validators = null;
	
	/**
	 * Whether or not to abort on the first failure
	 */
	private boolean failfast = true;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public ComplexDataObject process(ComplexDataObject item) throws Exception {
		ValidationManager mgr = validators.get();
		if(mgr == null) {
			mgr = new ValidationManager();
			validators.set(mgr);
		}
		try {
			if(failfast) {
				mgr.validateByException(item);
			} else {
				// Capture all failures
				final Collection<ValidationEvent> events = new LinkedList<ValidationEvent>();
				
				ValidationListener listener = new ValidationListener() {
		            public void validationPassed(ValidationEvent ve) {
		            }
	
		            public void validationFailed(ValidationEvent ve) {
		                events.add(ve);
		            }
		        };
		        
		        mgr.addValidationListener(listener);
		        
		        try {
			        if(!mgr.validateByEvents(item)) {
			        	if(events.size() == 1) {
			        		// Treat it as though we were validating by exception
			        		mgr.setEventBased(false);
			        		mgr.fireValidationEvent(events.iterator().next());
			        	} else {
			        		throw new C24CompoundValidationException(item, events);
			        	}
			        } 
		        } finally {
		        	mgr.removeValidationListener(listener);
		        }
			}
		} catch(ValidationException vEx) {
			throw new C24ValidationException("Failed to validate message: " + vEx.getLocalizedMessage(), item, vEx);
		}
		return item;
	}

	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		validators = new ThreadLocal<ValidationManager>();
		
	}

	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {

		
	}

	@Override
	public void close() throws ItemStreamException {
		validators = null;
		
	}
	

}
