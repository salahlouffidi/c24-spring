package biz.c24.io.spring.integration.transformer;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.examples.models.basic.Employees;
import biz.c24.io.spring.integration.C24Headers;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ValidatingHeaderEnricherTests {

	@Test
	public void testSingleValid() {
		Employee employee = new Employee();
		employee.setSalutation("Mr");
		employee.setFirstName("Andy");
		employee.setLastName("Acheson");
		employee.setJobTitle("Software Developer");
        employee.setSalary(BigDecimal.valueOf(55000));
		
		C24ValidatingHeaderEnricher enricher = new C24ValidatingHeaderEnricher();
		enricher.setAddFailEvents(true);
		enricher.setAddPassEvents(true);
		enricher.setAddStatistics(false);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>)enricher.transform(MessageBuilder.withPayload(employee).build());
		MessageHeaders headers = result.getHeaders();
		assertThat(headers.containsKey(C24Headers.VALID), is(true));
		assertThat(headers.get(C24Headers.VALID, Boolean.class), is(true));
		
		assertThat(headers.containsKey(C24Headers.FAIL_EVENTS), is(true));
		assertThat(headers.get(C24Headers.FAIL_EVENTS, Collection.class).size(), is(0));
		
		assertThat(headers.containsKey(C24Headers.PASS_EVENTS), is(true));
		assertThat(headers.get(C24Headers.PASS_EVENTS, Collection.class).size(), is(greaterThan(0)));

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
        joe.setSalary(BigDecimal.valueOf(45000));

        employees.addEmployee(joe);
					
		C24ValidatingHeaderEnricher enricher = new C24ValidatingHeaderEnricher();
		enricher.setAddFailEvents(true);
		enricher.setAddPassEvents(true);
		enricher.setAddStatistics(false);
		
		@SuppressWarnings("unchecked")
		Message<Employees> result = (Message<Employees>)enricher.transform(MessageBuilder.withPayload(employees).build());
		MessageHeaders headers = result.getHeaders();
		assertThat(headers.containsKey(C24Headers.VALID), is(true));
		assertThat(headers.get(C24Headers.VALID, Boolean.class), is(true));
		
		assertThat(headers.containsKey(C24Headers.FAIL_EVENTS), is(true));
		assertThat(headers.get(C24Headers.FAIL_EVENTS, Collection.class).size(), is(0));

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
		
		C24ValidatingHeaderEnricher enricher = new C24ValidatingHeaderEnricher();
		enricher.setAddFailEvents(true);
		enricher.setAddPassEvents(true);
		enricher.setAddStatistics(false);
		
		@SuppressWarnings("unchecked")
		Message<Employee> result = (Message<Employee>)enricher.transform(MessageBuilder.withPayload(employee).build());
		MessageHeaders headers = result.getHeaders();
		assertThat(headers.containsKey(C24Headers.VALID), is(true));
		assertThat(headers.get(C24Headers.VALID, Boolean.class), is(false));
		
		assertThat(headers.containsKey(C24Headers.FAIL_EVENTS), is(true));
		assertThat(headers.get(C24Headers.FAIL_EVENTS, Collection.class).size(), is(2));
	
	}
	
}
