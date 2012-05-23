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
package biz.c24.io.spring.batch.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import biz.c24.io.api.presentation.Sink;
import biz.c24.io.spring.batch.writer.C24ItemWriter;
import biz.c24.io.spring.batch.writer.source.WriterSource;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/*
 * Validate the C24ItemWriterParser
 * 
 * @author Andrew Elmore
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("item-writer.xml")
public class C24ItemWriterParserTests {
	
	@Autowired
	@Qualifier("ioItemWriter")
	private C24ItemWriter ioItemWriter;
	
	@Autowired
	private WriterSource fileWriterSource;
	
	@Autowired
	private Sink textualSink;
	
	@Test
	public void validateParser() {
		
		assertNotNull(ioItemWriter);
		
		assertThat(ioItemWriter.getWriterSource(), is(fileWriterSource));
		assertThat(ioItemWriter.getSink(), is(textualSink));

	}
	

}
