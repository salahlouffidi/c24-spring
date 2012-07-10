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
package biz.c24.io.spring.batch.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.poi.hssf.record.formula.functions.Ipmt;
import org.junit.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import biz.c24.io.api.presentation.TextualSink;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.spring.batch.writer.source.FileWriterSource;
import biz.c24.io.spring.batch.writer.source.ZipFileWriterSource;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Validate the C24ItemWriter
 * 
 * @author Andrew Elmore
 */
public class C24ItemWriterTests {
	
	@SuppressWarnings("serial")
	private List<Employee> employees = new LinkedList<Employee>() {{	
		add(new Employee() {{
			setFirstName("Andy");
			setLastName("Acheson");
			setJobTitle("Barman");
		}});
	
		add(new Employee() {{
			setFirstName("Steven");
			setLastName("Blair");
			setJobTitle("Professional Golfer");
		}});
		
		add(new Employee() {{
			setFirstName("Matthew");
			setLastName("Richardson");
			setJobTitle("Fireman");
		}});
	}};
	
	@Test
	public void testFileWrite() throws Exception {

		String outputFileName = null;
		
		try {
			// Get somewhere temporary to write out to
			outputFileName = File.createTempFile("ItemWriterTest-", ".csv").getAbsolutePath();
		
			// Configure the ItemWriter
			C24ItemWriter itemWriter = new C24ItemWriter();		
			itemWriter.setSink(new TextualSink());
			itemWriter.setWriterSource(new FileWriterSource());
			itemWriter.setup(getStepExecution(outputFileName));
			// Write the employees out
			itemWriter.write(employees);
			// Close the file
			itemWriter.cleanup();
	
			// Check that we wrote out what was expected
			FileInputStream inputStream = new FileInputStream(outputFileName);
			try {
				compareCsv(inputStream, employees);
			} finally {
				if(inputStream != null) {
					inputStream.close();
				}
			}
			
		} finally {
			if(outputFileName != null) {
				// Clear up our temporary file
				File file = new File(outputFileName);
				file.delete();
			}
		}
		
	}
	
	@Test
	public void testZipFileWrite() throws Exception {

		String outputFileName = null;
		
		try {
			// Get somewhere temporary to write out to
			outputFileName = File.createTempFile("ItemWriterTest-", ".csv.zip").getAbsolutePath();
		
			// Configure the ItemWriter
			C24ItemWriter itemWriter = new C24ItemWriter();		
			itemWriter.setSink(new TextualSink());
			itemWriter.setWriterSource(new ZipFileWriterSource());
			itemWriter.setup(getStepExecution(outputFileName));
			// Write the employees out
			itemWriter.write(employees);
			// Close the file
			itemWriter.cleanup();
	
			// Check that we wrote out what was expected
			ZipFile zipFile = new ZipFile(outputFileName);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			assertNotNull(entries);
			// Make sure there's at least one entry
			assertTrue(entries.hasMoreElements());
			ZipEntry entry = entries.nextElement();
			// Make sure that the trailing .zip has been removed and the leading path has been removed
			assertFalse(entry.getName().contains(System.getProperty("file.separator")));
			assertFalse(entry.getName().endsWith(".zip"));
			// Make sure that there aren't any other entries
			assertFalse(entries.hasMoreElements());
			
			try {
				compareCsv(zipFile.getInputStream(entry), employees);
			} finally {
				if(zipFile != null) {
					zipFile.close();
				}
			}
			
		} finally {
			if(outputFileName != null) {
				// Clear up our temporary file
				File file = new File(outputFileName);
				file.delete();
			}
		}
		
	}
	
	/**
	 * Utility method to check that the contents of a CSV employee file match the list of employees we used to generate it
	 * 
	 * @param fileName The file to read
	 * @param employees The list of employees we expect to read from the file
	 */
	private void compareCsv(InputStream inputStream, List<Employee> employees) throws IOException {
		BufferedReader reader = null;
		
		reader = new BufferedReader(new InputStreamReader(inputStream));
		
		for(Employee employee : employees) {
			if(!reader.ready()) {
				fail("File contained insufficient rows");
			}
			String line = reader.readLine();
			String expected = employee.getFirstName() + "," + employee.getLastName() + "," + employee.getJobTitle();
			assertThat(line, is(expected));
		}
		
		if(reader.ready()) {
			fail("File contained more data than expected");
		}
	}
	
	
	/**
	 * Mock up the necessary job parameters
	 * 
	 * @param outputFileName The filename we want the ItemWriter to write to	
	 */
	private StepExecution getStepExecution(String outputFileName) throws IOException {
		
		JobParameters jobParams = mock(JobParameters.class);
		when(jobParams.getString("output.file")).thenReturn(outputFileName);

		StepExecution stepExecution = mock(StepExecution.class);
		when(stepExecution.getJobParameters()).thenReturn(jobParams);
		
		return stepExecution;
		
	}
}
