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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file-splitter.xml")
public class FileSplitterTests {

    @Resource(name = "feed-channel")
    PollableChannel feedChannel;

    @Resource(name = "result-channel")
    PollableChannel resultChannel;

    @Test
    public void mytest2() throws Exception {


        Message<List<String>> message = (Message<List<String>>) feedChannel.receive();
        assertThat(message.getPayload(), is(not(nullValue())));
        assertThat(message.getPayload().get(0), is("1"));
        assertThat(message.getPayload().get(4), is("5"));

        message = (Message<List<String>>) feedChannel.receive();
        assertThat(message.getPayload(), is(not(nullValue())));
        assertThat(message.getPayload().get(0), is("6"));
        assertThat(message.getPayload().get(4), is("10"));

        Message<Boolean> resultMmessage = (Message<Boolean>) resultChannel.receive();
        assertThat(resultMmessage.getPayload(), is(true));
    }

}
