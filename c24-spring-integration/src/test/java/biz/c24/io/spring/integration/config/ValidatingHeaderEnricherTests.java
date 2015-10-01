/*
 * Copyright 2012 the original author or authors.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.spring.integration.C24Headers;

/**
 * @author Andrew Elmore
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("validating-header-enricher.xml")
public class ValidatingHeaderEnricherTests extends BaseIntegrationTest {

	@Autowired
	@Qualifier("inputChannel")
	MessageChannel inputChannel;

	private MessagingTemplate template;

	@Autowired
	@Qualifier("outputChannel")
	PollableChannel outputChannel;

	// These are for the routing tests
	@Autowired
	@Qualifier("routingInputChannel")
	MessageChannel routingInputChannel;
	@Autowired
	@Qualifier("unclassifiedErrorChannel")
	PollableChannel unclassifiedErrorChannel;
	@Autowired
	@Qualifier("invalidFirstNameChannel")
	PollableChannel invalidFirstNameChannel;

	
	
	@Before
	public void before() {
		template = new MessagingTemplate(inputChannel);
	}
	
	@Test
	public void testValid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		employee.setFirstName("Andy");
		employee.setLastName("Acheson");
		employee.setJobTitle("Software Developer");
        employee.setSalary(BigDecimal.valueOf(55000));

        template.convertAndSend(employee);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>) outputChannel.receive(1);
		
		assertThat(result, is(not(nullValue())));
		
		MessageHeaders headers = result.getHeaders();
		assertThat(headers.containsKey(C24Headers.VALID), is(true));
		assertThat(headers.get(C24Headers.VALID, Boolean.class), is(true));
		
		assertThat(headers.containsKey(C24Headers.FAIL_EVENTS), is(true));
		assertThat(headers.get(C24Headers.FAIL_EVENTS, Collection.class).size(), is(0));
		
		assertThat(headers.containsKey(C24Headers.PASS_EVENTS), is(true));
		assertThat(headers.get(C24Headers.PASS_EVENTS, Collection.class).size(), is(greaterThan(0)));

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
        employee.setSalary(BigDecimal.valueOf(55000));

        template.convertAndSend(employee);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>) outputChannel.receive(1);
		
		assertThat(result, is(not(nullValue())));
		
		MessageHeaders headers = result.getHeaders();
		assertThat(headers.containsKey(C24Headers.VALID), is(true));
		assertThat(headers.get(C24Headers.VALID, Boolean.class), is(false));
		
		assertThat(headers.containsKey(C24Headers.FAIL_EVENTS), is(true));
		assertThat(headers.get(C24Headers.FAIL_EVENTS, Collection.class).size(), is(2));
		
		assertThat(headers.containsKey(C24Headers.PASS_EVENTS), is(true));
		assertThat(headers.get(C24Headers.PASS_EVENTS, Collection.class).size(), is(greaterThan(0)));

	}
	
	
	@Test
	public void testRouting() {
		
		Employee validEmployee = new Employee();
		validEmployee.setSalutation("Mr");
		validEmployee.setFirstName("Andy");
		validEmployee.setLastName("Acheson");
		validEmployee.setJobTitle("Software Developer");
        validEmployee.setSalary(BigDecimal.valueOf(55000));

        template.convertAndSend(routingInputChannel, validEmployee);
		
		// Check that it showed up where we expected
		assertThat(outputChannel.receive(1), is(not(nullValue())));
		assertThat(unclassifiedErrorChannel.receive(1), is(nullValue()));
		assertThat(invalidFirstNameChannel.receive(1), is(nullValue()));
		
		Employee invalidEmployee = new Employee();
		invalidEmployee.setSalutation("Mr");
		invalidEmployee.setFirstName("Andy");
		invalidEmployee.setLastName("Acheson");
		// Invalid - no job title
		//invalidEmployee.setJobTitle("Software Developer");
        invalidEmployee.setSalary(BigDecimal.valueOf(55000));

        template.convertAndSend(routingInputChannel, invalidEmployee);
		
		// Check that it showed up where we expected
		assertThat(outputChannel.receive(1), is(nullValue()));
		assertThat(unclassifiedErrorChannel.receive(1), is(not(nullValue())));
		assertThat(invalidFirstNameChannel.receive(1), is(nullValue()));
		
		// Now also give the employee an invalid first name; this should cause us to flow down the other path
		invalidEmployee.setFirstName("andy");
		
		template.convertAndSend(routingInputChannel, invalidEmployee);
		
		// Check that it showed up where we expected
		assertThat(outputChannel.receive(1), is(nullValue()));
		assertThat(unclassifiedErrorChannel.receive(1), is(nullValue()));
		assertThat(invalidFirstNameChannel.receive(1), is(not(nullValue())));
		
		
		
	}



}
