package biz.c24.io.spring.integration.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("XPathRouter_noDefaultChannel.xml")
public class XPathRouterNoDefaultChannelTests extends BaseIntegrationTest {

    @Autowired
    MessageChannel textInputChannel;

    private MessagingTemplate template;

    @Before
    public void before() {
        template = new MessagingTemplate(textInputChannel);
    }

    @Test (expected = MessageDeliveryException.class)
    public void noOutputChannel() throws Exception {
        template.convertAndSend(loadCsvBytes());

    }
}
