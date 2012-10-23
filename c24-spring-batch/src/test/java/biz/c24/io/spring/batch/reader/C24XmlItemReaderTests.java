/*
 * Copyright 2012 C24 Technologies
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
package biz.c24.io.spring.batch.reader;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.ClassPathResource;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.examples.models.basic.EmployeeElement;
import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
import biz.c24.io.spring.batch.reader.source.FileSource;
import biz.c24.io.spring.batch.reader.source.ZipFileSource;
import biz.c24.io.spring.core.C24Model;
import biz.c24.io.spring.source.SourceFactory;
import biz.c24.io.spring.source.TextualSourceFactory;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Validate the C24ItemReader
 * 
 * @author Andrew Elmore
 */
public class C24XmlItemReaderTests {
	
	private C24Model employeeModel = new C24Model(EmployeeElement.getInstance());
	private C24Model employeeXmlModel = new C24Model(biz.c24.io.examples.models.xml.EmployeeElement.getInstance());

	@Test
	public void testValidXmlRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-valid.xml"));
		
		// Validation & splitting
		Collection<ComplexDataObject> objs = readFile(employeeXmlModel, "(?s)<employee .*", "(?s).*/>.*", true, source);
		assertThat(objs.size(), is(3));		
	}
	
	private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, BufferedReaderSource source) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException {
		return readFile(model, optionalElementStartRegEx, optionalElementStopRegEx, validate, source, null);
	}

	private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, BufferedReaderSource source, SourceFactory factory) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException { 
		C24XmlItemReader<ComplexDataObject> reader = new C24XmlItemReader<ComplexDataObject>();
		reader.setModel(model);
		if(optionalElementStartRegEx != null) {
			reader.setElementStartPattern(optionalElementStartRegEx);
		}
		if(optionalElementStopRegEx != null) {
			reader.setElementStopPattern(optionalElementStopRegEx);
		}
		if(factory != null) {
			reader.setSourceFactory(factory);
		}
		
		reader.setSource(source);
		reader.setValidate(validate);
		
		StepExecution stepExecution = getStepExecution();
		
		reader.setup(stepExecution);

		ComplexDataObject obj = null;
		Collection<ComplexDataObject> objs = new LinkedList<ComplexDataObject>();
		
		while((obj = reader.read()) != null) {
			assertThat(obj.getDefiningElementDecl(), is(model.getRootElement()));
			objs.add(obj);
		}
		
		reader.cleanup();
		
		return objs;
	}
		
	private StepExecution getStepExecution() throws IOException {
		
		JobParameters jobParams = mock(JobParameters.class);

		StepExecution stepExecution = mock(StepExecution.class);
		when(stepExecution.getJobParameters()).thenReturn(jobParams);
		
		return stepExecution;
		
	}
}
