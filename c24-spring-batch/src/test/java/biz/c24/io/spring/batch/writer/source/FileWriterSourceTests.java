package biz.c24.io.spring.batch.writer.source;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.core.io.FileSystemResource;


public class FileWriterSourceTests {

    @Test
    public void testNoResource() throws IOException {
        // Confirm that FileWriterSource defaults to using output.file from the job parameters if we don't
        // specify a resource
        
        // Get somewhere temporary to write out to    
        File outputFile = File.createTempFile("ItemWriterTest-", ".csv");
        outputFile.deleteOnExit();
        String outputFileName = outputFile.getAbsolutePath();

        FileWriterSource source = new FileWriterSource();
        
        JobParameters params = mock(JobParameters.class);
        when(params.getString("output.file")).thenReturn(outputFileName);
        
        StepExecution execution = mock(StepExecution.class);
        when(execution.getJobParameters()).thenReturn(params);
        
        source.initialise(execution);
        
        final String testString = "testDefaultResource";
        source.getWriter().write(testString);
        source.close();
        
        // Read the file back and confirm it contains the test string
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        assertThat(reader.readLine(), is(testString));
        reader.close();
    }
    
    @Test
    public void testResource() throws IOException {
        // Confirm that FileWriterSource uses the resource if we supply one
        
        // Get somewhere temporary to write out to    
        File outputFile = File.createTempFile("ItemWriterTest-", ".csv");
        outputFile.deleteOnExit();
        String outputFileName = outputFile.getAbsolutePath();
        
        FileSystemResource resource = new FileSystemResource(outputFileName);

        FileWriterSource source = new FileWriterSource();
        source.setResource(resource);
        
        // Mock up JobParams without output.file
        JobParameters params = mock(JobParameters.class);
        StepExecution execution = mock(StepExecution.class);
        when(execution.getJobParameters()).thenReturn(params);
        
        source.initialise(execution);
        
        final String testString = "testDefaultResource";
        source.getWriter().write(testString);
        source.close();
        
        // Read the file back and confirm it contains the test string
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        assertThat(reader.readLine(), is(testString));
        reader.close();
    }
    
}
