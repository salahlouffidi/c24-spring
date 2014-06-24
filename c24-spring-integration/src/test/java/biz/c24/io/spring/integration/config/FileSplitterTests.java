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

import biz.c24.io.spring.integration.transformer.C24FileSplittingTransformer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.Message;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.integration.support.MessageBuilder;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class FileSplitterTests {

    PollableChannel feedChannel;
    C24FileSplittingTransformer transformer;
    ClassPathResource resource;

    @Before
    public void setUp() {
        feedChannel = new QueueChannel(100);
        transformer = new C24FileSplittingTransformer(feedChannel);
        resource = new ClassPathResource("datafixtures/10-lines.txt");
    }

    @Test
    public void defaultBatchSize() throws Exception {
        Boolean result = (Boolean) transformer.doTransform(MessageBuilder.withPayload((resource.getFile())).build());
        Message<List<String>> message;
        int messageCount = 0;
        for (int i = 0; i < 10; i++) {
            message = (Message<List<String>>) feedChannel.receive();
            assertThat(message.getPayload().get(0), is(i + 1 + ""));
            messageCount++;
        }
        assertThat(messageCount, is(10));

    }

    @Test
    public void splitFileInTwo() throws Exception {

        transformer.setBatchSize(5);
        Boolean result = (Boolean)transformer.doTransform(MessageBuilder.withPayload((resource.getFile())).build());
        Message<List<String>> message = (Message<List<String>>) feedChannel.receive();
        assertThat(message.getPayload(), is(not(nullValue())));
        assertThat(message.getPayload().get(0), is("1"));
        assertThat(message.getPayload().get(4), is("5"));

        message = (Message<List<String>>) feedChannel.receive();
        assertThat(message.getPayload(), is(not(nullValue())));
        assertThat(message.getPayload().get(0), is("6"));
        assertThat(message.getPayload().get(4), is("10"));

        assertThat(result, is(true));
    }

    @Test
    public void splitOnEvens() throws Exception {
        transformer.setInitiator("^-?\\d*[02468]$");
        Boolean result = (Boolean)transformer.doTransform(MessageBuilder.withPayload((resource.getFile())).build());
        Message<List<String>> message;
        int messageCount = 0;
        for (int i = 0; i < 5; i++) {
            message = (Message<List<String>>) feedChannel.receive();
            if(i < 4) {
                assertThat(message.getPayload().get(0), is((i + 1) * 2 + "" + ((i + 1) * 2 + 1)));
            } else {
                assertThat(message.getPayload().get(0), is("10"));
            }
            messageCount++;
        }
        assertThat(messageCount, is(5));
    }

}
