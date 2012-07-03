/*
 * Copyright 2011 the original author or authors.
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
package biz.c24.io.spring.integration.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import biz.c24.io.api.data.ValidationEvent;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.spring.integration.validation.C24AggregatedMessageValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("validating-selector.xml")
public class ValidatingSelectorTests extends BaseIntegrationTest {

	@Autowired
	@Qualifier("textInputChannel")
	MessageChannel textInputChannel;
	
	@Autowired
	@Qualifier("exceptionThrowingInputChannel")
	MessageChannel exceptionThrowingInputChannel;

	private MessagingTemplate template;

	@Autowired
	@Qualifier("validChannel")
	PollableChannel validChannel;
	
	@Autowired
	@Qualifier("invalidChannel")
	PollableChannel invalidChannel;

	@Before
	public void before() {
		template = new MessagingTemplate(textInputChannel);
	}
	
	@Test
	public void testValid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		employee.setFirstName("Andy");
		employee.setLastName("Acheson");
		employee.setJobTitle("Software Developer");
		
		template.convertAndSend(employee);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>) validChannel.receive(1);
		
		assertThat(result, is(not(nullValue())));
		assertThat(result.getPayload(), is(Employee.class));
		
		// Make sure there are no other messages floating around
		assertThat(validChannel.receive(1), is(nullValue()));
		assertThat(invalidChannel.receive(1), is(nullValue()));
	}

	@Test
	public void testInvalid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		// Invalid as first char not capitalised
		employee.setFirstName("andy");
		employee.setLastName("Acheson");
		// Invalid as no job title
		//employee.setJobTitle("Software Developer");
		
		template.convertAndSend(employee);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>) invalidChannel.receive(1);
		
		assertThat(result, is(not(nullValue())));
		assertThat(result.getPayload(), is(Employee.class));
		
		// Make sure there are no other messages floating around
		assertThat(validChannel.receive(1), is(nullValue()));
		assertThat(invalidChannel.receive(1), is(nullValue()));

	}
	
	@Test
	public void testThrowingInvalid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		// Invalid as first char not capitalised
		employee.setFirstName("andy");
		employee.setLastName("Acheson");
		// Invalid as no job title
		//employee.setJobTitle("Software Developer");
		
		try {
			template.convertAndSend(exceptionThrowingInputChannel, employee);
			fail("Selector failed to throw exception on invalid message");
		} catch(MessageHandlingException ex) {
			// Expected behaviour
			assertThat(ex.getCause(), is(C24AggregatedMessageValidationException.class));
			C24AggregatedMessageValidationException vEx = (C24AggregatedMessageValidationException) ex.getCause();
			ListIterator<ValidationEvent> failures = vEx.getFailEvents();
			int failureCount = 0;
			while(failures.hasNext()) {
				failureCount++;
				failures.next();
			}
			assertThat(failureCount, is(2));
			
		}
		
		// Make sure there are no other messages floating around
		assertThat(validChannel.receive(1), is(nullValue()));
		assertThat(invalidChannel.receive(1), is(nullValue()));

	}



}
