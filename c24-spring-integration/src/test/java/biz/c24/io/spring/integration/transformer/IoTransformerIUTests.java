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
package biz.c24.io.spring.integration.transformer;

import static biz.c24.io.spring.integration.test.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import biz.c24.io.examples.models.basic.Email;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.examples.models.basic.InputDocumentRootElement;
import biz.c24.io.examples.models.basic.OutputDocumentRoot;
import biz.c24.io.examples.transforms.basic.ExampleTransform;
import biz.c24.io.examples.transforms.basic.EmployeeToEmailTransform;
import biz.c24.io.spring.core.C24Model;


public class IoTransformerIUTests {

	C24Model model = new C24Model(InputDocumentRootElement.getInstance());

	@Test
	public void canTransform() throws Exception {

		C24Transformer transformer = new C24Transformer();
		transformer.setTransformClass(ExampleTransform.class);

		Message<?> message = MessageBuilder.withPayload(loadObject()).build();

		Message<?> outputMessage = transformer.transform(message);

		assertThat(outputMessage.getPayload(), notNullValue());
		assertThat(outputMessage.getPayload(), is(OutputDocumentRoot.class));


	}
	
	public static class MyEmail {
		
		private String firstNameInitial;
	    private String surname;
	    private String domainName;
		
	    public String getFirstNameInitial() {
			return firstNameInitial;
		}
		public void setFirstNameInitial(String firstNameInitial) {
			this.firstNameInitial = firstNameInitial;
		}
		public String getSurname() {
			return surname;
		}
		public void setSurname(String surname) {
			this.surname = surname;
		}
		public String getDomainName() {
			return domainName;
		}
		public void setDomainName(String domainName) {
			this.domainName = domainName;
		}
	}
	
	@Test
	public void canTransformToPojo() throws Exception {
		
		Employee employee = new Employee();
		employee.setFirstName("Tom");
		employee.setLastName("Smith");
		employee.setJobTitle("Porter");
		
		C24Transformer transformer = new C24Transformer();
		transformer.setTransformClass(EmployeeToEmailTransform.class);
		
		Message<?> message = MessageBuilder.withPayload(employee).build();
		
		Message<?> result = transformer.transform(message);
		
		assertNotNull(result);
		assertThat(result.getPayload(), is(Email.class));

		transformer.setTargetClass(MyEmail.class);
		
		result = transformer.transform(message);
		
		assertNotNull(result);
		assertThat(result.getPayload(), is(MyEmail.class));
		MyEmail email = (MyEmail) result.getPayload();
		assertThat(email.getFirstNameInitial(), is("T."));
		assertThat(email.getSurname(), is("Smith"));
		assertThat(email.getDomainName(), is("@company.com"));
		
	}

}
