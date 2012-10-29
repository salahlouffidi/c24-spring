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
package biz.c24.io.spring.batch.reader.source;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.core.io.Resource;

import biz.c24.io.spring.util.C24Utils;

/**
 * An implementation of SplittingReaderSource which extracts its data from uncompressed files.
 * Expects to be told the path of the file to write to by the supplied Resource or, 
 * if not specified, a property called input.file in the job parameters
 * (as populated by Spring Batch's org.springframework.batch.admin.integration.FileToJobLaunchRequestAdapter)
 * 
 * @author Andrew Elmore
 */
public class FileSource implements SplittingReaderSource {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileSource.class);
	
	private SplittingReader reader = null;
	
	private String name;
	
	private Resource resource = null;
	
	private String encoding = C24Utils.DEFAULT_FILE_ENCODING;
	
	/**
	 * How many lines at the start of the file should we skip?
	 */
	private int skipLines = 0;
	
	/*
	 * (non-Javadoc)
	 * @see biz.c24.io.spring.batch.reader.source.SplittingReaderSource#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#initialise(org.springframework.batch.core.StepExecution)
	 */
	public void initialise(StepExecution stepExecution) {
	    
        try {
    	    // Get an InputStream and a name for where we're reading from
    	    // Use the Resource if supplied
    	    
    	    InputStream source = null;
    	    if(resource != null) {
    	        name = resource.getFilename();
    	        source = resource.getInputStream();
    	    } else {
    	        
    	        // If no resource supplied, fallback to a Job parameter called input.file
    	        name = stepExecution.getJobParameters().getString("input.file");
    	        
    	        // Remove any leading file:// if it exists
    	        if(name.startsWith("file://")) {
    	            name = name.substring("file://".length());
    	        }
    	      
    	        source = new FileInputStream(name);   
    	    }
    
			// Prime the reader
    	    LOG.debug("Opening {} with encoding {}", name, getEncoding());
			reader = new SplittingReader(new InputStreamReader(source, getEncoding()), true);
			if(skipLines > 0) {
				for(int i = 0; i < skipLines && reader.ready(); i++) {
					// Skip the line
					reader.readLine();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#close()
	 */
	public void close() {
		if(reader != null) {
			try {
				reader.close();
				// Spring Batch lifecycle will ensure that this doesn't happen while 
				// someone is still trying to read (ie calling getReader and risking an NPE)
				reader = null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#getReader()
	 */
	public SplittingReader getReader() {
		try {
			if(reader != null && reader.ready()) {
				return reader;
			} else {
				return null;
			}
		} catch (IOException e) {
			// Stream has been closed beneath our feet. Nothing to read.
			return null;
		}
	}

	@Override
	public SplittingReader getNextReader() {
	    SplittingReader retVal = reader;
		reader = null;
		return retVal;
	}

	@Override
	public boolean useMultipleThreadsPerReader() {
		return true;
	}

	@Override
	public void discard(SplittingReader reader) throws IOException {
		if(this.reader == reader) {
			reader.close();
			this.reader = null;
		}
		
	}

	/**
	 * How many lines will be skipped at the start of the file before the Reader is handed to callers?
	 * @return the number of lines to skip at the start of the file
	 */
	public int getSkipLines() {
		return skipLines;
	}

	/**
	 * How many lines should be skipped at the start of the file before the Reader is handed to callers?
	 * @param skipLines
	 */
	public void setSkipLines(int skipLines) {
		this.skipLines = skipLines;
	}

	/**
	 * The resource we acquire InputStreams from
	 * @return the resource that we'll read from
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Set the resource we acquire InputStreams from
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Returns the encoding we are using when reading the file.
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

