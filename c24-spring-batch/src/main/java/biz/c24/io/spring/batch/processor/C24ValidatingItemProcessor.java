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

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationManager;

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
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public ComplexDataObject process(ComplexDataObject item) throws Exception {
		ValidationManager vm = validators.get();
		if(vm == null) {
			vm = new ValidationManager();
			validators.set(vm);
		}
		vm.validateByException(item);
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
