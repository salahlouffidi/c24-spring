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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.springframework.batch.core.StepExecution;

/*
 * WriterSource that writes all output to a single file.
 * Expects to be told the path of the file to write to by the parameter output.file in the job parameters.
 * 
 * @author Andrew Elmore
 */
public class FileWriterSource implements WriterSource {

	private FileWriter outputFile = null;

	@Override
	public void initialise(StepExecution stepExecution) {
		// Extract the name of the file we're supposed to be writing to
	    String fileName = stepExecution.getJobParameters().getString("output.file");
	    
	    // Remove any leading file:// if it exists
	    if(fileName.startsWith("file://")) {
	    		fileName = fileName.substring("file://".length());
	    }
	
	    try {
	    	outputFile = new FileWriter(fileName);
	    } catch(IOException ioEx) {
	    	throw new RuntimeException(ioEx);
	    }
		
	}

	@Override
	public void close() {
		if(outputFile != null)  {
			try {
				outputFile.close();
			} catch(IOException ioEx) {
		    	throw new RuntimeException(ioEx);
		    } finally {
		    	outputFile = null;
		    }
		}	
	}

	@Override
	public Writer getWriter() {
		return outputFile;
	}
}
