package biz.c24.io.spring.batch.config;

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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("item-reader.xml")
public class C24ItemReaderParserTest {
	
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
	
	private void validateReader(C24ItemReader reader, String expectedStartPattern, boolean expectedValidate, 
								Class<? extends BufferedReaderSource> expectedSource) {
		
		assertThat(reader.getElementStartPattern(), is(expectedStartPattern));
		assertThat(reader.isValidating(), is(expectedValidate));
		assertThat(reader.getElementType(), is(employeeElement));
		assertThat(reader.getSource(), instanceOf(expectedSource));

		
	}
	
	@Test
	public void validateParser() {
		
		validateReader(nonSplittingNonValidatingCsvReader, null, false, FileSource.class);
		validateReader(nonSplittingValidatingCsvReader, null, true, FileSource.class);
		validateReader(splittingNonValidatingCsvReader, ".*", false, FileSource.class);
		validateReader(splittingValidatingCsvReader, ".*", true, FileSource.class);
		validateReader(nonSplittingValidatingZipReader, null, true, ZipFileSource.class);
		validateReader(splittingValidatingZipReader, ".*", true, ZipFileSource.class);
	}
	

}
