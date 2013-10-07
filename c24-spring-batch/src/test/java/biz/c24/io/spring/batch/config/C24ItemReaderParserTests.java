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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.examples.models.basic.Employee;
import biz.c24.io.examples.models.basic.EmployeeElement;
import biz.c24.io.spring.batch.reader.C24ItemReader;
import biz.c24.io.spring.batch.reader.source.SplittingReaderSource;
import biz.c24.io.spring.batch.reader.source.FileSource;
import biz.c24.io.spring.batch.reader.source.ZipFileSource;
import biz.c24.io.spring.source.SourceFactory;
import biz.c24.io.spring.source.XmlSourceFactory;
import biz.c24.io.spring.util.C24Utils;
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
	private C24ItemReader<Employee> nonSplittingNonValidatingCsvReader;

	@Autowired
	@Qualifier("nonSplittingValidatingCsvReader")
	private C24ItemReader<Employee> nonSplittingValidatingCsvReader;
	
	@Autowired
	@Qualifier("splittingValidatingCsvReader")
	private C24ItemReader<Employee> splittingValidatingCsvReader;
	
	@Autowired
	@Qualifier("splittingFullyValidatingCsvReader")
	private C24ItemReader<Employee> splittingFullyValidatingCsvReader;
	
	@Autowired
	@Qualifier("splittingNonValidatingCsvReader")
	private C24ItemReader<Employee> splittingNonValidatingCsvReader;
	
	@Autowired
	@Qualifier("nonSplittingValidatingZipReader")
	private C24ItemReader<Employee> nonSplittingValidatingZipReader;
	
	@Autowired
	@Qualifier("splittingValidatingZipReader")
	private C24ItemReader<Employee> splittingValidatingZipReader;
	
	@Autowired
	@Qualifier("xmlSourceFactoryReader")
	private C24ItemReader<biz.c24.io.examples.models.xml.Employee> xmlSourceFactoryReader;
	
	@Autowired
	@Qualifier("fileSourceReader")
	private C24ItemReader<Employee> fileSourceReader;
	
    @Autowired
    @Qualifier("zipFileSourceReader")
    private C24ItemReader<Employee> zipFileSourceReader;
    
    @Autowired
    @Qualifier("fileSourceResourceReader")
    private C24ItemReader<Employee> fileSourceResourceReader;
    
    @Autowired
    @Qualifier("zipFileSourceResourceReader")
    private C24ItemReader<Employee> zipFileSourceResourceReader;
 	
	
	private void validateReader(C24ItemReader<? extends ComplexDataObject> reader, String expectedStartPattern, String expectedStopPattern, boolean expectedValidate, 
								Class<? extends SplittingReaderSource> expectedSource) {
		validateReader(reader, expectedStartPattern, expectedStopPattern, expectedValidate, true, expectedSource, null);
	}
		
	private void validateReader(C24ItemReader<? extends ComplexDataObject> reader, String expectedStartPattern, String expectedStopPattern, boolean expectedValidate, 
				boolean failfast, Class<? extends SplittingReaderSource> expectedSource, Class<? extends SourceFactory> expectedSourceFactory) {
		
		assertThat(reader.getElementStartPattern(), is(expectedStartPattern));
		assertThat(reader.getElementStopPattern(), is(expectedStopPattern));
		assertThat(reader.isValidating(), is(expectedValidate));
		assertThat(reader.isFailfast(), is(failfast));
		assertThat(reader.getElementType(), is(employeeElement));
		assertThat(reader.getSource(), instanceOf(expectedSource));
		assertThat(reader.getSourceFactory(), expectedSourceFactory == null? nullValue() : instanceOf(expectedSourceFactory));

		
	}
	
    private void validateSource(SplittingReaderSource source, Class<? extends SplittingReaderSource> expectedClass, int expectedSkipLines, Class<? extends Resource> expectedResource) {
        validateSource(source, expectedClass, expectedSkipLines, expectedResource, C24Utils.DEFAULT_FILE_ENCODING, true);
    }
	
	private void validateSource(SplittingReaderSource source, Class<? extends SplittingReaderSource> expectedClass, int expectedSkipLines, Class<? extends Resource> expectedResource, 
	        String expectedEncoding, boolean expectedConsistentLineTerminators) {
	    assertThat(source, is(expectedClass));
	    if(source instanceof FileSource) {
	        FileSource fileSource = (FileSource)source;
	        assertThat(fileSource.getSkipLines(), is(expectedSkipLines));
	        assertThat(fileSource.getResource(), expectedResource != null? is(expectedResource) : nullValue());
	        assertThat(fileSource.getEncoding(), is(expectedEncoding));
	        assertThat(fileSource.isConsistentLineTerminators(), is(expectedConsistentLineTerminators));
	    } else if(source instanceof ZipFileSource) {
            ZipFileSource fileSource = (ZipFileSource)source;
            assertThat(fileSource.getSkipLines(), is(expectedSkipLines));
            assertThat(fileSource.getResource(), expectedResource != null? is(expectedResource) : nullValue());
            assertThat(fileSource.getEncoding(), is(expectedEncoding));
            assertThat(fileSource.isConsistentLineTerminators(), is(expectedConsistentLineTerminators));
        }
	}
	
	@Test
	public void validateReaderParser() {
		
		validateReader(nonSplittingNonValidatingCsvReader, null, null, false, FileSource.class);
		validateReader(nonSplittingValidatingCsvReader, null, null, true, FileSource.class);
		validateReader(splittingNonValidatingCsvReader, ".*", null, false, FileSource.class);
		validateReader(splittingValidatingCsvReader, ".*", null, true, FileSource.class);
		validateReader(splittingFullyValidatingCsvReader, ".*", null, true, false, FileSource.class, null);
		validateReader(nonSplittingValidatingZipReader, null, null, true, ZipFileSource.class);
		validateReader(splittingValidatingZipReader, ".*", null, true, ZipFileSource.class);
		validateReader(xmlSourceFactoryReader, "^[ \t]*<[a-zA-Z].*", "^[ \t]*</.*", true, true, FileSource.class, XmlSourceFactory.class);
		validateReader(fileSourceReader, null, null, false, FileSource.class);
        validateReader(fileSourceResourceReader, null, null, false, FileSource.class);
        validateReader(zipFileSourceReader, null, null, false, ZipFileSource.class);
        validateReader(zipFileSourceResourceReader, null, null, false, ZipFileSource.class);
	}
	
	@Test
	public void validateSourceParser() {
        validateSource(fileSourceReader.getSource(), FileSource.class, 0, null, "UTF-8", false);
        validateSource(fileSourceResourceReader.getSource(), FileSource.class, 5, UrlResource.class, "TestEncoding", true);
        validateSource(zipFileSourceReader.getSource(), ZipFileSource.class, 0, null, "UTF-8", false);
        validateSource(zipFileSourceResourceReader.getSource(), ZipFileSource.class, 4, UrlResource.class, "TestEncoding", true);	    
	}
	

}
