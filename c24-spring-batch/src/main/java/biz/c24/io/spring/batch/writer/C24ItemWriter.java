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
package biz.c24.io.spring.batch.writer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.Sink;
import biz.c24.io.spring.batch.writer.source.WriterSource;

/**
 * ItemWriter that sinks and writes ComplexDataObjects to a Writer.
 * 
 * Allows concurrent calls to write but synchronises on individual CDO write to the writer.
 * 
 * @author Andrew Elmore
 */
public class C24ItemWriter implements ItemWriter<ComplexDataObject>{
	
	private Sink templateSink = null;
	private ThreadLocal<Sink> sink = new ThreadLocal<Sink>();
	private WriterSource writerSource = null;

	
	/**
	 * Asserts that the object has been properly configured
	 */
	@PostConstruct
	public void validateConfiguration() {
		Assert.notNull(templateSink, "Sink must be set");
		Assert.notNull(writerSource, "WriterSource must be set");
	}
	
	/**
	 * Initialise our context
	 * 
	 * @param stepExecution The step execution context
	 */
	@BeforeStep
	public void setup(StepExecution stepExecution) {	
		writerSource.initialise(stepExecution);
	}
	
	
	/**
	 * Clean up any resources we're consuming
	 */
	@AfterStep
	public void cleanup() {
		writerSource.close();
	}
	
	/**
	 * Writes the contents of the StringWriter to our output file
	 * 
	 * @param writer The StringWriter to read the data from
	 */
	private void write(Sink sink) throws IOException {
		
		StringWriter writer = (StringWriter)sink.getWriter();
		
		writer.flush();
		
		StringBuffer buffer = writer.getBuffer();
		
		// Sadly StringBuffer doesn't allow us read-only access to its internal array, so we have to copy
		String element = buffer.toString();
		
		Writer outputWriter = writerSource.getWriter();
		synchronized(outputWriter) {
			outputWriter.write(element);
		}
		
		// Reset the buffer for next time
		buffer.setLength(0);
		
	}

	/**
	 * Get a thread-safe Sink
	 */
	private Sink getThreadsafeSink() {
		
		Sink sink = this.sink.get();
		
		if(sink == null) {
			// First time this thread has used a sink; create one
			sink = (Sink)templateSink.clone();
			sink.setWriter(new StringWriter());
			this.sink.set(sink);
		}	
		
		return sink;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends ComplexDataObject> items) throws Exception {
		
		// Get a sink to use
		Sink sink = getThreadsafeSink();

		
		for(ComplexDataObject cdo : items) {
			// Sink the CDO
			sink.writeObject(cdo);
		}
		
		// Now write the whole lot out
		write(sink);

	}
	
	/**
	 * The prototype sink used by this C24ItemWriter
	 * 
	 * @returns The prototype sink
	 */
	public Sink getSink() {
		return templateSink;
	}
	
	/**
	 * Provides a prototype sink for this C24ItemWriter to use when sinking ComplexDataObjects
	 * 
	 * @param sink The prototype sink
	 */
	@Required
	public void setSink(Sink sink) {
		templateSink = sink;
	}
	
	/**
	 * Gets the WriterSource used by this C24ItemWriter to get a Writer to persist sunk ComplexDataObjects to
	 * 
	 * @returns The WriterSource used by this C24ItemWriter
	 */
	public WriterSource getWriterSource() {
		return writerSource;
	}
	
	/**
	 * Sets the WriterSource that this C24ItemWriter will use to get a Writer to write sunk ComplexDataObjects to
	 * 
	 * @param writerSource The WriterSource to use
	 */
	@Required
	public void setWriterSource(WriterSource writerSource) {
		this.writerSource = writerSource;
	}
	
}
