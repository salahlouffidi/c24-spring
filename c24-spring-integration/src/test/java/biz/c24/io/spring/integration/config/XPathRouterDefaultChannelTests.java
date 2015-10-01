package biz.c24.io.spring.integration.config;

import biz.c24.io.examples.models.basic.Employees;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("XPathRouter_defaultChannel.xml")
public class XPathRouterDefaultChannelTests extends BaseIntegrationTest {

    @Autowired
    MessageChannel textInputChannel;

    @Autowired
    @Qualifier("defaultChannel")
    PollableChannel defaultChannel;

    private MessagingTemplate template;

    @Before
    public void before() {
        template = new MessagingTemplate(textInputChannel);
    }

    @Test
    public void receiveOnDefaultChannel() throws Exception {
        template.convertAndSend(loadCsvBytes());

        Message<?> message = defaultChannel.receive(1);

        assertThat(message.getPayload(), notNullValue());
        assertThat(message.getPayload(), instanceOf(Employees.class));

    }
}