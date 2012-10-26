package biz.c24.io.spring.batch.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.ClassPathResource;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.examples.models.basic.EmployeeElement;
import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
import biz.c24.io.spring.batch.reader.source.ZipFileSource;
import biz.c24.io.spring.core.C24Model;
import biz.c24.io.spring.source.SourceFactory;

/**
 * Validate the C24ItemReader works correctly in parallel
 * 
 * @author Andrew Elmore
 *
 */
public class C24ItemReaderParallelTests {
    private C24Model employeeModel = new C24Model(EmployeeElement.getInstance());
    private C24Model employeeXmlModel = new C24Model(biz.c24.io.examples.models.xml.EmployeeElement.getInstance());

    
    @Test
    public void testValidZipSingleRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
        
        ZipFileSource source = new ZipFileSource();
        source.setResource(new ClassPathResource("employees-100-valid-individual-noparent.xml.zip"));
        
        // No validation, no splitting
        Collection<ComplexDataObject> objs = readFile(employeeXmlModel, null, null, false, source);
        assertThat(objs.size(), is(100));
        
        // Validation but no splitting
        objs = readFile(employeeXmlModel, null, null, true, source);
        assertThat(objs.size(), is(100));
        assertThat(source.useMultipleThreadsPerReader(), is(false));
        
        // Validation & splitting - start pattern only
        objs = readFile(employeeXmlModel, ".*<employee.*", null, true, source);
        assertThat(objs.size(), is(100));
        assertThat(source.useMultipleThreadsPerReader(), is(false));

        // Validation & splitting - start & stop patterns
        objs = readFile(employeeXmlModel, ".*<employee.*", ".*/>.*", true, source);
        assertThat(objs.size(), is(100));
        assertThat(source.useMultipleThreadsPerReader(), is(false));
    }
    
    @Test
    public void testValidZipCombinedRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
        
        ZipFileSource source = new ZipFileSource();
        source.setResource(new ClassPathResource("employees-1500-valid-combined.xml.zip"));

        // Validation & splitting - start pattern only
        Collection<ComplexDataObject> objs = readFile(employeeXmlModel, ".*<employee .*", ".*/>.*", true, source);
        assertThat(objs.size(), is(1500));
        assertThat(source.useMultipleThreadsPerReader(), is(true));
    }
    
    @Test
    public void testValidZipCombinedNoParentRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
        
        ZipFileSource source = new ZipFileSource();
        source.setResource(new ClassPathResource("employees-1500-valid-combined-noparent.xml.zip"));
        
        // Validation & splitting - start pattern only
        Collection<ComplexDataObject> objs = readFile(employeeXmlModel, ".*<employee .*", null, true, source);
        assertThat(objs.size(), is(1500));
        assertThat(source.useMultipleThreadsPerReader(), is(true));

        // Validation & splitting - start & stop patterns
        objs = readFile(employeeXmlModel, ".*<employee .*", ".*/>.*", true, source);
        assertThat(objs.size(), is(1500));
        assertThat(source.useMultipleThreadsPerReader(), is(true));
    }
    
    
    private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, BufferedReaderSource source) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException {
        return readFile(model, optionalElementStartRegEx, optionalElementStopRegEx, validate, source, null);
    }

    private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, String optionalElementStopRegEx, boolean validate, BufferedReaderSource source, SourceFactory factory) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException { 
        C24ItemReader<ComplexDataObject> reader = new C24ItemReader<ComplexDataObject>();
        reader.setModel(model);
        if(optionalElementStartRegEx != null) {
            reader.setElementStartPattern(optionalElementStartRegEx);
        }
        if(optionalElementStopRegEx != null) {
            reader.setElementStopPattern(optionalElementStopRegEx);
        }
        if(factory != null) {
            reader.setSourceFactory(factory);
        }
        
        reader.setSource(source);
        reader.setValidate(validate);
        
        StepExecution stepExecution = getStepExecution();
        
        reader.setup(stepExecution);

        Collection<ComplexDataObject> objs = Collections.synchronizedList(new LinkedList<ComplexDataObject>());
        
        Thread[] threads = new Thread[8];
        for(int i=0; i < threads.length; i++) {
            threads[i] = new Thread(new Worker<ComplexDataObject>(model, objs, reader));
            threads[i].start();
        }
        
        for(int i=0; i < threads.length; i++) {
            try {
                threads[i].join(20000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted waiting on thread");
            }
            if(threads[i].isAlive()) {
                throw new RuntimeException("Timed out waiting for thread to complete its work");
            }
        }
        
        reader.cleanup();
        
        return objs;
    }
    
    private static class Worker<T extends ComplexDataObject> implements Runnable {
        private C24Model model;
        private Collection<T> collection;
        private ItemReader<T> reader;
        
        public Worker(C24Model model, Collection<T> collection, ItemReader<T> itemReader) {
            this.model = model;
            this.collection = collection;
            this.reader = itemReader;
        }
        
        public void run() {
            T obj = null;
            try {
                while((obj = reader.read()) != null) {
                    assertThat(obj.getDefiningElementDecl(), is(model.getRootElement()));
                    collection.add(obj);                  
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
        
    private StepExecution getStepExecution() throws IOException {
        
        JobParameters jobParams = mock(JobParameters.class);

        StepExecution stepExecution = mock(StepExecution.class);
        when(stepExecution.getJobParameters()).thenReturn(jobParams);
        
        return stepExecution;
        
    }
    
}
