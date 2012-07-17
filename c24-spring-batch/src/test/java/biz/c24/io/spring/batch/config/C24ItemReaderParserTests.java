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

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import biz.c24.io.api.data.Element;
import biz.c24.io.examples.models.basic.EmployeeElement;
import biz.c24.io.spring.batch.reader.C24ItemReader;
import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
import biz.c24.io.spring.batch.reader.source.FileSource;
import biz.c24.io.spring.batch.reader.source.ZipFileSource;
import biz.c24.io.spring.source.SourceFactory;
import biz.c24.io.spring.source.TextualSourceFactory;
import biz.c24.io.spring.source.XmlSourceFactory;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Validate the C24ItemReaderParser
 * 
 * @author Andrew Elmore
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("item-reader.xml")
public class C24ItemReaderParserTests {
	
	private Element employeeElement = EmployeeElement.getInstance();
	
	@Autowired
	@Qualifier("nonSplittingNonValidatingCsvReader")
	private C24ItemReader nonSplittingNonValidatingCsvReader;

	@Autowired
	@Qualifier("nonSplittingValidatingCsvReader")
	private C24ItemReader nonSplittingValidatingCsvReader;
	
	@Autowired
	@Qualifier("splittingValidatingCsvReader")
	private C24ItemReader splittingValidatingCsvReader;
	
	@Autowired
	@Qualifier("splittingNonValidatingCsvReader")
	private C24ItemReader splittingNonValidatingCsvReader;
	
	@Autowired
	@Qualifier("nonSplittingValidatingZipReader")
	private C24ItemReader nonSplittingValidatingZipReader;
	
	@Autowired
	@Qualifier("splittingValidatingZipReader")
	private C24ItemReader splittingValidatingZipReader;
	
	@Autowired
	@Qualifier("xmlSourceFactoryReader")
	private C24ItemReader xmlSourceFactoryReader;
	
	
	
	private void validateReader(C24ItemReader reader, String expectedStartPattern, boolean expectedValidate, 
								Class<? extends BufferedReaderSource> expectedSource) {
		validateReader(reader, expectedStartPattern, expectedValidate, expectedSource, null);
	}
		
	private void validateReader(C24ItemReader reader, String expectedStartPattern, boolean expectedValidate, 
				Class<? extends BufferedReaderSource> expectedSource, Class<? extends SourceFactory> expectedSourceFactory) {
		
		assertThat(reader.getElementStartPattern(), is(expectedStartPattern));
		assertThat(reader.isValidating(), is(expectedValidate));
		assertThat(reader.getElementType(), is(employeeElement));
		assertThat(reader.getSource(), instanceOf(expectedSource));
		assertThat(reader.getSourceFactory(), expectedSourceFactory == null? nullValue() : instanceOf(expectedSourceFactory));

		
	}
	
	@Test
	public void validateParser() {
		
		validateReader(nonSplittingNonValidatingCsvReader, null, false, FileSource.class);
		validateReader(nonSplittingValidatingCsvReader, null, true, FileSource.class);
		validateReader(splittingNonValidatingCsvReader, ".*", false, FileSource.class);
		validateReader(splittingValidatingCsvReader, ".*", true, FileSource.class);
		validateReader(nonSplittingValidatingZipReader, null, true, ZipFileSource.class);
		validateReader(splittingValidatingZipReader, ".*", true, ZipFileSource.class);
		validateReader(xmlSourceFactoryReader, ".*", true, FileSource.class, XmlSourceFactory.class);

	}
	

}
