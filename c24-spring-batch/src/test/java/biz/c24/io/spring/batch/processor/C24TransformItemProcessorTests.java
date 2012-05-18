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
package biz.c24.io.spring.batch.processor;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.transform.Transform;
import biz.c24.io.examples.models.basic.Email;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.examples.transforms.basic.EmployeeToEmailTransform;

/*
 * Test the C24TransformItemProcessor
 * 
 * @author Andrew Elmore
 */
public class C24TransformItemProcessorTests {
	
	private Transform employeeToEmail = new EmployeeToEmailTransform();
	
	
	@Test
	public void testValidTransform() throws Exception {
		Employee validEmployee = new Employee();
		
		validEmployee.setFirstName("Dave");
		validEmployee.setLastName("Taylor");
		validEmployee.setSalutation("Mr");
		validEmployee.setJobTitle("Compliance Officer");
		
		C24TransformItemProcessor transformer = new C24TransformItemProcessor();
		transformer.setTransformer(employeeToEmail);
		transformer.setValidation(true);
		
		ComplexDataObject email = transformer.process(validEmployee);
		
		assertThat(email, instanceOf(Email.class));
		
	}
	
	
	@Test
	public void testInvalidTransform() throws Exception {
		Employee employee = new Employee();
		
		employee.setFirstName("Dave");
		// Use of @ is invalid in an email address
		employee.setLastName("T@ylor");
		employee.setSalutation("Mr");
		employee.setJobTitle("Compliance Officer");
		
		C24TransformItemProcessor transformer = new C24TransformItemProcessor();
		transformer.setTransformer(employeeToEmail);
		transformer.setValidation(false);
		
		// Validation is off so this should succeed
		ComplexDataObject email = transformer.process(employee);
		assertThat(email, instanceOf(Email.class));
		
		transformer.setValidation(true);
		try {
			transformer.process(employee);
			fail("C24TransformItemProcessor failed to detect invalid CDO");
		} catch(ValidationException vEx) {
			// Expected behaviour
		}
		
	}
	

}
