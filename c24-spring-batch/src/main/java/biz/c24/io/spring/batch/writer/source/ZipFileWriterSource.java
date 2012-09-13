/*
 * Copyright 2012 C24 Technologies.
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
package biz.c24.io.spring.batch.writer.source;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.batch.core.StepExecution;
import org.springframework.core.io.FileSystemResource;

import biz.c24.io.spring.util.C24Utils;

/**
 * WriterSource that writes all output to a single zip file. All data is written to a single entry in the zip file.
 * Expects to be told the path of the file to write to by the supplied Resource or, 
 * if not specified, by the parameter output.file in the job parameters.
 * 
 * @author Andrew Elmore
 */
public class ZipFileWriterSource implements WriterSource {

	private OutputStreamWriter outputWriter = null;
	private FileSystemResource resource = null;
	private ZipOutputStream zipStream = null;
	private static String pathSepString = System.getProperty("file.separator");
	private String encoding = C24Utils.DEFAULT_FILE_ENCODING;

	@Override
	public void initialise(StepExecution stepExecution) {
		// Extract the name of the file we're supposed to be writing to
        String fileName = resource != null? resource.getPath() : stepExecution.getJobParameters().getString("output.file");
	    
	    // Remove any leading file:// if it exists
	    if(fileName.startsWith("file://")) {
	    		fileName = fileName.substring("file://".length());
	    }
	    
	    // Now create the name of our zipEntry
	    // Strip off the leading path and the suffix (ie the zip extension)
	    int tailStarts = fileName.lastIndexOf(pathSepString) + 1;
	    int tailEnds = fileName.lastIndexOf('.');
	    if(tailStarts < 0) {
	    	tailStarts = 0;
	    }
	    if(tailEnds < 0) {
	    	tailEnds = fileName.length();
	    }
	    
	    
	    String tailName = fileName.substring(tailStarts, tailEnds);
	
	    try {
	    	FileOutputStream fileStream = new FileOutputStream(fileName);
	    	zipStream = new ZipOutputStream(fileStream);
	    	zipStream.putNextEntry(new ZipEntry(tailName));
	    	outputWriter = new OutputStreamWriter(zipStream, getEncoding());
	    } catch(IOException ioEx) {
	    	throw new RuntimeException(ioEx);
	    }
		
	}

	@Override
	public void close() {
		if(outputWriter != null)  {
			try {
				outputWriter.close();
			} catch(IOException ioEx) {
		    	throw new RuntimeException(ioEx);
		    } finally {
		    	outputWriter = null;
		    }
		}
		if(zipStream != null)  {
			try {
				zipStream.close();
			} catch(IOException ioEx) {
		    	throw new RuntimeException(ioEx);
		    } finally {
		    	zipStream = null;
		    }
		}
	}

	@Override
	public Writer getWriter() {
		return outputWriter;
	}
	
    /**
     * The resource we use to determine our output path
     * @return the resource this ZipFileWriterSource will write to
     */
    public FileSystemResource getResource() {
        return resource;
    }

    /**
     * Set the resource we acquire our output path from
     */
    public void setResource(FileSystemResource resource) {
        this.resource = resource;
    }  
    
    /**
     * Returns the encoding we are using when reading the file.
     * Defaults to UTF-8
     * @return the encoding being used to read the file
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding to use to read the file
     * @param encoding the encoding the use
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }   
}
