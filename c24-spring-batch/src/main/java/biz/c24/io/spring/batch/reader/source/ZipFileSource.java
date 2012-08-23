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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.batch.core.StepExecution;
import org.springframework.core.io.Resource;

/**
 * An implementation of BufferedReaderSource which extracts its data from Zip files.
 * Expects to get the file name from a property called input.file in the job parameters
 * (as populated by Spring Batch's org.springframework.batch.admin.integration.FileToJobLaunchRequestAdapter)
 * 
 * @author Andrew Elmore
 */
public class ZipFileSource implements BufferedReaderSource {
	
	/**
	 * The name of the zip file we're reading from
	 */
	private String name;
	
	/**
	 * The current BufferedReader to be returned in calls to getReader if not exhausted
	 */
	private volatile BufferedReader reader = null;
	
	/**
	 * The underlying zipFile
	 */
	private ZipFile zipFile;
	
	/**
	 * An iterator over the entries in the zip file
	 */
	private Enumeration<? extends ZipEntry> zipEntries;

	/**
	 * A hint to our users; should they use multiple threads on a single reader or ask us
	 * for a different reader for each thread?
	 */
	private boolean useMultipleThreadsPerReader = true;
	
	/**
	 * How many lines at the start of the file should we skip?
	 */
	private int skipLines = 0;
	
	/**
	 * The Resource we acquire InputStreams from
	 */
	private Resource resource;
	
	/*
	 * (non-Javadoc)
	 * @see biz.c24.io.spring.batch.reader.source.BufferedReaderSource#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#initialise(org.springframework.batch.core.StepExecution)
	 */
	public void initialise(StepExecution stepExecution) {
        
        name = resource.getDescription();

		try {
			zipFile = new ZipFile(resource.getFile());
			zipEntries = zipFile.entries();
			ZipEntry entry = null;
			if(zipEntries.hasMoreElements()) {
				entry = zipEntries.nextElement();
				// Prime the reader
				reader = getReader(entry);
			}
			
			// If we have a large number of ZipEntries and the first one looks relatively small, advise 
			// callers to use a thread per reader
			if(entry != null && zipFile.size() > 20 && (entry.getSize() == -1 || entry.getSize() < 10000)) {
				useMultipleThreadsPerReader = false;
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#close()
	 */
	public void close() {
		if(zipFile != null) {
			try {
				zipFile.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private BufferedReader getReader(ZipEntry entry) throws IOException {
		BufferedReader newReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
		if(skipLines > 0) {
			for(int i = 0; i < skipLines && newReader.ready(); i++) {
				// Skip the line
				newReader.readLine();
			}
		}
		return newReader;
	}
	
	/* (non-Javadoc)
	 * @see biz.c24.spring.batch.BufferedReaderSource#getReader()
	 */
	public BufferedReader getReader() {
		try {

			if(reader != null && !reader.ready()) {
				synchronized(this) {
					// Multiple threads could be calling this in parallel; check the work hasn't already been performed for us
					if(reader != null && !reader.ready()) {
						// Our current reader is exhausted...
						if(zipEntries.hasMoreElements()) {
							// ... but there are more files to process in the zip file
							try {
								reader = getReader(zipEntries.nextElement());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}		
						} else {
							// We've processed all the files in the zip file
							reader = null;
						}
					}
				}
			}
			
			return reader;
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized BufferedReader getNextReader() {
		BufferedReader retVal = reader;
		
		if(retVal != null) {
			// Set up the next reader to return
			if(zipEntries.hasMoreElements()) {
				try {
					reader = getReader(zipEntries.nextElement());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				reader = null;
			}
		}
		
		return retVal;

	}

	@Override
	public boolean useMultipleThreadsPerReader() {
		return useMultipleThreadsPerReader;
	}

	@Override
	public synchronized void discard(Reader reader) throws IOException {
		if(this.reader == reader) {
			getNextReader();
		}	
		reader.close();
	}
	
	/**
	 * How many lines will be skipped at the start of the file before the Reader is handed to callers?
	 * @return
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
	 * @return
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Set the resource we acquire InputStreams from
	 * @return
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}	
	
	
}

