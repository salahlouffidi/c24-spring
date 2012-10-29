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
import biz.c24.io.spring.batch.reader.source.SplittingReaderSource;
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
public class C24ItemReaderTests {
	
	private C24Model employeeModel = new C24Model(EmployeeElement.getInstance());
	private C24Model employeeXmlModel = new C24Model(biz.c24.io.examples.models.xml.EmployeeElement.getInstance());

	@Test
	public void testValidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-valid.csv"));
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source);
		assertThat(objs.size(), is(3));
		
		// Validation but no splitting
		objs = readFile(employeeModel, null, null, true, source);
		assertThat(objs.size(), is(3));
		
		// Validation & splitting
		objs = readFile(employeeModel, ".*", null, true, source);
		assertThat(objs.size(), is(3));
	}
   

    @Test
    public void testValidXmlRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
        FileSource source = new FileSource();
        source.setResource(new ClassPathResource("employee-valid-noparent.xml"));
        
        // No validation, no splitting
        Collection<ComplexDataObject> objs = readFile(employeeXmlModel, null, null, false, source);
        assertThat(objs.size(), is(1));  
    }
    
	
	@Test
	public void testValidSplittingXmlRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-valid.xml"));
		
		// Validation & splitting
		Collection<ComplexDataObject> objs = readFile(employeeXmlModel, "^[ \t]*<employee .*", ".*/>.*", true, source);
		assertThat(objs.size(), is(3));		
	}
	
	@Test
	public void testSourceFactory() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-valid.csv"));
		
		TextualSourceFactory factory = new TextualSourceFactory();
		factory.setEndOfDataRequired(false);
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source, factory);
		assertThat(objs.size(), is(3));
		
		// Validation but no splitting
		objs = readFile(employeeModel, null, null, true, source, factory);
		assertThat(objs.size(), is(3));
		
		// Validation & splitting
		objs = readFile(employeeModel, ".*", null, true, source, factory);
		assertThat(objs.size(), is(3));
	}
	
	
	@Test
	public void testSemanticallyInvalidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-semanticallyinvalid.csv"));
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source);
		assertThat(objs.size(), is(3));

		// Validation but no splitting
		try {
			readFile(employeeModel, null, null, true, source);
			fail("Semantically invalid file did not generate a ValidationException");
		} catch(C24ValidationException pEx) {
			// Expected behaviour
		}
		
		// Validation & splitting
		// Validation but no splitting
		try {
			readFile(employeeModel, ".*", null, true, source);
			fail("Semantically invalid file did not generate a ValidationException");
		} catch(C24ValidationException pEx) {
			// Expected behaviour
		}
	}
	
	
	@Test
	public void testStructurallyInvalidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		FileSource source = new FileSource();
		source.setResource(new ClassPathResource("employees-3-structurallyinvalid.csv"));
		
		// No validation, no splitting
		try {
			readFile(employeeModel, null, null, false, source);
			fail("Structurally invalid file did not generate a ParserException");
		} catch(ParseException uiEx) {
			// Expected behaviour
		}
		
		// Validation but no splitting
		try {
			readFile(employeeModel, null, null, true, source);
			fail("Structurally invalid file did not generate a ParserException");
		} catch(ParseException uiEx) {
			// Expected behaviour
		}
		
		// Validation & splitting
		try {
			readFile(employeeModel, ".*", null, true, source);
			fail("Structurally invalid file did not generate a ParserException");
		} catch(ParseException uiEx) {
			// Expected behaviour
		}
	}
	
	@Test
	public void testValidZipRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		
		ZipFileSource source = new ZipFileSource();
		source.setResource(new ClassPathResource("employees-5-valid.zip"));
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source);
		assertThat(objs.size(), is(5));
		
		// Validation but no splitting
		objs = readFile(employeeModel, null, null, true, source);
		assertThat(objs.size(), is(5));
		assertThat(source.useMultipleThreadsPerReader(), is(true));
		
		// Validation & splitting
		objs = readFile(employeeModel, ".*", null, true, source);
		assertThat(objs.size(), is(5));
		assertThat(source.useMultipleThreadsPerReader(), is(true));
		
		// Now give is a zip file with lots of small entries. Check that it encourages 1 thread per ZipEntry
		source.setResource(new ClassPathResource("employees-50-valid.zip"));
		objs = readFile(employeeModel, ".*", null, true, source);
		assertThat(objs.size(), is(50));
		assertThat(source.useMultipleThreadsPerReader(), is(false));
	}
	
	// Header Skipping Tests
	@Test
	public void testValidCsvHeaderRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		
		FileSource source = new FileSource();
		source.setSkipLines(1);
		source.setResource(new ClassPathResource("employees-3-valid-header.csv"));
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source);
		assertThat(objs.size(), is(3));
		
		// Validation but no splitting
		objs = readFile(employeeModel, null, null, true, source);
		assertThat(objs.size(), is(3));
		
		// Validation & splitting
		objs = readFile(employeeModel, ".*", null, true, source);
		assertThat(objs.size(), is(3));
	}
	
	@Test
	public void testValidZipHeaderRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
		
		ZipFileSource source = new ZipFileSource();
		source.setSkipLines(1);
		source.setResource(new ClassPathResource("employees-5-valid-header.zip"));
		
		// No validation, no splitting
		Collection<ComplexDataObject> objs = readFile(employeeModel, null, null, false, source);
		assertThat(objs.size(), is(5));
		
		// Validation but no splitting
		objs = readFile(employeeModel, null, null, true, source);
		assertThat(objs.size(), is(5));
		assertThat(source.useMultipleThreadsPerReader(), is(true));
		
		// Validation & splitting
		objs = readFile(employeeModel, ".*", null, true, source);
		assertThat(objs.size(), is(5));
		assertThat(source.useMultipleThreadsPerReader(), is(true));
	}
	
	private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, SplittingReaderSource source) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException {
		return readFile(model, optionalElementStartRegEx, optionalElementStopRegEx, validate, source, null);
	}

	private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, SplittingReaderSource source, SourceFactory factory) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException { 
		C24ItemReader<ComplexDataObject> reader = new C24ItemReader<ComplexDataObject>();
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
