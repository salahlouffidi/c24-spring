package biz.c24.io.spring.integration.jdbc;


import biz.c24.io.examples.models.basic.Employee;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class CdoSqlParameterSourceTests {

    @Test
    public void withCdoPayload() {
        Employee employee = new Employee();
        employee.setFirstName("Tom");
        employee.setLastName("Smith");
        employee.setJobTitle("Porter");
        employee.setSalutation("Mr");
        Message<?> message = MessageBuilder.withPayload(employee).build();
        CdoSqlParameterSource parameterSource = (CdoSqlParameterSource) new CdoSqlParameterSourceFactory().createParameterSource(message);
        assertTrue(parameterSource.hasValue("FirstName"));
        assertTrue(parameterSource.hasValue("LastName"));
        assertTrue(parameterSource.hasValue("JobTitle"));
        assertTrue(parameterSource.hasValue("Salutation"));
        assertFalse(parameterSource.hasValue("Foo"));
        assertThat(parameterSource.getValue("FirstName").toString(), is("Tom"));
        assertThat(parameterSource.getValue("LastName").toString(), is("Smith"));
        assertThat(parameterSource.getValue("JobTitle").toString(), is("Porter"));
        assertThat(parameterSource.getValue("Salutation").toString(), is("Mr"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void withNonCdoPayload() {
        new CdoSqlParameterSourceFactory().createParameterSource(MessageBuilder.withPayload(new String("Not a CDO")));
    }

    @Test (expected = IllegalArgumentException.class)
    public void withNonMessageInput() {
        new CdoSqlParameterSourceFactory().createParameterSource(new String("Not a Spring Message"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void withNullMessage() {
        new CdoSqlParameterSourceFactory().createParameterSource(null);
    }

}
