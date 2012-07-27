package biz.c24.io.spring.batch.reader;

import java.io.IOException;
import java.io.Reader;

import org.springframework.batch.item.ParseException;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.presentation.Source;
import biz.c24.io.api.presentation.XMLSource;

/**
 * Parser
 * A wrapper class to manage parsing from iO sources. 
 * The Parser hierarchy exists primarily for 2 reasons:
 * 
 * 1. To hide the decision on what to synchronise from the parsing logic; we simply select the correct type
 * of parser based on configuration.
 * 
 * 2. A single parser (hence iO source) might be shared between multiple threads. With a pure iO source, if we fail to
 * parse an entity from the underlying reader, it will generate an exception but we have no way to tell other threads 
 * using the same iO source not to try and parse from the iO source again (which they will, and of course they will
 * also get an exception). Wrapping it in a parser allows us to store a status flag (finished) that, when such an event 
 * occurs, we can set to prevent further parsing attempts.
 * 
 * This base class does not synchronize on any of its methods.
 * 
 * @author andrew
 *
 */
class Parser {
	/**
	 * The underying iO source to read from
	 */
	private Source ioSource;
	
	/**
	 * A flag to control when we've finished reading from this parser
	 * Set when we encounter a parsing exception; we can't process any further
	 */
	private volatile boolean finished = false;
	
	/*
	 * The type of CDO that we're trying to parse from the underlying reader
	 */
	private Element element;
	
	/**
	 * Construct a parser from the supplied iO source to read the specified type of Element
	 * @param ioSource
	 * @param element
	 */
	public Parser(Source ioSource, Element element) {
		this.ioSource = ioSource;
		this.element = element;
	}
	
	/**
	 * Sets the reader that we'll consume data from
	 * @param reader
	 */
	public void setReader(Reader reader) {
		ioSource.setReader(reader);
	}
	
	public Reader getReader() {
		return ioSource.getReader();
	}
	
	/**
	 * Attempts to read a ComplexDataObject from the Reader
	 * @return A parsed ComplexDataObject
	 * @throws IOException
	 */
	public ComplexDataObject read() throws IOException {
		ComplexDataObject obj = null;
		
		if(!finished) {
			try {
				obj = ioSource.readObject(element);
			} catch(IOException ioEx) {
				
				// If we're using the XML source, the underlying SAXParser can helpfully close the stream
				// when it finished parsing the previous element, presumably because it assumes the document 
				// is well-formed (ie only one per file)
				if(ioSource instanceof XMLSource) {
					// Find the root cause
					Throwable ex = ioEx;
					while(ex.getCause() != null) {
						ex = ex.getCause();
					}
					if(ex instanceof IOException && ex.getMessage() == "Stream closed") {
						// Sigh. That looks like that's what's happened.
						obj = null;
					} else {
						// Rethrow
						throw ioEx;
					}
				} else {
					// Rethrow
					throw ioEx;
				}
			} finally {
				if(obj == null) {
					finished = true;
				}
			}
		} 

		return obj;
	}

}
