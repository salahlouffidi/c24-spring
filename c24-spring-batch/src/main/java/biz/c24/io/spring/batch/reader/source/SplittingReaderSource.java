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

import java.io.IOException;

import org.springframework.batch.core.StepExecution;

/**
 * Interface to abstract away details of the actual source of SplittingReader.
 * 
 * @author Andrew Elmore
 */
public interface SplittingReaderSource {
	
	/**
	 * Get an identifier for the underlying source of SplittingReader
	 */
	public abstract String getName();

	/**
	 * Initialise the SplittingReaderSource
	 * 
	 * @param stepExecution
	 */
	public abstract void initialise(StepExecution stepExecution);

	/**
	 * Close the source, releasing any held resources
	 */
	public abstract void close();

	/**
	 * Return a SplittingReader to provide access to the data in the SplittingReaderSource.
	 * The SplittingReader returned can change between calls.
	 * 
	 * @return A SplittingReader
	 */
	public abstract SplittingReader getReader();
	
	/**
	 * Returns the next available reader if any.
	 * Allows multiple threads to process multiple readers before the prior one is exhausted
	 * 
	 * @return A SplittingReader
	 */
	public abstract SplittingReader getNextReader();
	
	/**
	 * Indicates to a reader whether or not it should use multiple threads per reader
	 * 
	 * @return True iff it should use multiple threads per reader
	 */
	public abstract boolean useMultipleThreadsPerReader();
	
	/**
	 * Discard the supplied reader and ensure that it is not returned in any future calls
	 * 
	 * @param reader The SplittingReader to be discarded
	 */
	public abstract void discard(SplittingReader reader) throws IOException;
}
