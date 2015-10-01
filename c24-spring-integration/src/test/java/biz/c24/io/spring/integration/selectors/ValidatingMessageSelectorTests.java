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
package biz.c24.io.spring.integration.selectors;

import java.math.BigDecimal;
import java.util.ListIterator;

import org.junit.Test;
import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.support.MessageBuilder;

import biz.c24.io.api.data.ValidationEvent;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.examples.models.basic.Employees;
import biz.c24.io.spring.integration.selector.C24ValidatingMessageSelector;
import biz.c24.io.spring.integration.validation.C24AggregatedMessageValidationException;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Andrew Elmore
 */
public class ValidatingMessageSelectorTests {
	
	@Test
	public void testSingleValid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		employee.setFirstName("Andy");
		employee.setLastName("Acheson");
		employee.setJobTitle("Software Developer");
        employee.setSalary(BigDecimal.valueOf(55000));

        C24ValidatingMessageSelector selector = new C24ValidatingMessageSelector();
		assertThat(selector.accept(MessageBuilder.withPayload(employee).build()), is(true));
		
	}
	
	
	@Test
	public void testMultipeValid() {
		Employees employees = new Employees();
		
		Employee andy = new Employee();
		andy.setSalutation("Mr");
		andy.setFirstName("Andy");
		andy.setLastName("Acheson");
		andy.setJobTitle("Software Developer");
        andy.setSalary(BigDecimal.valueOf(55000));

        employees.addEmployee(andy);
		
		Employee joe = new Employee();
		joe.setSalutation("Mr");
		joe.setFirstName("Joe");
		joe.setLastName("Bloggs");
		joe.setJobTitle("Security Guard");
        joe.setSalary(BigDecimal.valueOf(55000));

        employees.addEmployee(joe);
			
		
		C24ValidatingMessageSelector selector = new C24ValidatingMessageSelector();
		assertThat(selector.accept(MessageBuilder.withPayload(employees).build()), is(true));
		
	}
	
	@Test
	public void testInvalid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		// Should fail due to non-capitalised first letter
		employee.setFirstName("andy");
		employee.setLastName("Acheson");
		// No job title set - should also cause a validation failure
		//employee.setJobTitle("Software Developer");
        employee.setSalary(BigDecimal.valueOf(55000));

        C24ValidatingMessageSelector selector = new C24ValidatingMessageSelector();
		selector.setFailFast(true);
		selector.setThrowExceptionOnRejection(false);
		assertThat(selector.accept(MessageBuilder.withPayload(employee).build()), is(false));
		
		selector.setFailFast(false);
		assertThat(selector.accept(MessageBuilder.withPayload(employee).build()), is(false));
		
		selector.setFailFast(true);
		selector.setThrowExceptionOnRejection(true);
		try {
			selector.accept(MessageBuilder.withPayload(employee).build());
			fail("Selector failed to throw an exception on invalid CDO");
		} catch(MessageRejectedException ex) {
			// Expected behaviour
			assertThat(ex.getCause(), instanceOf(ValidationException.class));
		}
		
		selector.setFailFast(false);
		try {
			selector.accept(MessageBuilder.withPayload(employee).build());
			fail("Selector failed to throw an exception on invalid CDO");
		} catch(C24AggregatedMessageValidationException ex) {
			// Expected behaviour
			ListIterator<ValidationEvent> failures = ex.getFailEvents();
			int failureCount = 0;
			while(failures.hasNext()) {
				failureCount++;
				failures.next();
			}
			assertThat(failureCount, is(2));

		}		
	}
}
