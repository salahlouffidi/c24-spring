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
package biz.c24.io.spring.batch.reader;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.data.ValidationManager;
import biz.c24.io.api.presentation.Source;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.spring.batch.reader.source.SplittingReaderSource;
import biz.c24.io.spring.batch.reader.source.SplittingReader;
import biz.c24.io.spring.core.C24Model;
import biz.c24.io.spring.source.SourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * ItemReader that reads ComplexDataObjects from a SplittingReaderSource.
 * Optionally supports the ability to split the incoming data stream into entities by use of a
 * regular expression to detect the start of a new entity; this allows the more expensive parsing 
 * to be performed in parallel.
 * 
 * The optional splitting process currently assumes that each line:
 * a) Is terminated with a platform specific CRLF (or equivalent)
 * b) Belongs to at most one entity
 * 
 * In all cases the optional validation takes place in parallel if multiple threads are used.
 * 
 * @author Andrew Elmore
 */
public class C24ItemReader<Result> implements ItemReader<Result> {
	
	private static Logger LOG = LoggerFactory.getLogger(C24ItemReader.class);
	
	/**
	 * SourceFactory to use to generate our IO Sources
	 */
	private SourceFactory ioSourceFactory = null;
	
	/**
	 * Parser to use where we do not have an elementStartPattern
	 */
	private volatile Parser parser = null;
	/**
	 * Cache for parsers where we can parallelise parsing
	 */
	private ThreadLocal<Parser> threadedParser = new ThreadLocal<Parser>();
	
	/**
	 * The type of CDO that we will parse from the source
	 */
	private Element elementType;
	
	/**
	 * An optional pattern to use to quickly split the readerSource so we can perform more heavyweight
	 * parsing in parallel
	 */
	private Pattern elementStartPattern = null;
	
	/**
	 * An optional pattern to use to identify the end of a message. If specified, the message must end with an
	 * EOF or this pattern. Additional matches of the startPattern before presence of the stop pattern will
	 * not trigger the start of a new message
	 */
	private Pattern elementStopPattern = null;
	
	/**
	 * The source from which we'll read the data
	 */
	private SplittingReaderSource source;


	/**
	 * Control whether or not we validate the parsed CDOs
	 */
	private ThreadLocal<ValidationManager> validator = null;
	
	/**
	 * Allow clients to register a callback to intercept elements as we read them.
	 */
	private ParseListener<Object, Result> parseListener = null;
	
	
	public C24ItemReader() {

	}
	
	/**
	 * Asserts that we have been properly configured
	 */
	@PostConstruct
	public void validateConfiguration() {
		Assert.notNull(elementType, "Element type must be set, either explicitly or by setting the model");
		Assert.notNull(source, "Source must be set");
		if(elementStopPattern != null) {
			Assert.notNull(elementStartPattern, "elementStopPattern can only be used if an elementStartPattern is also set");
		}
	}
	
	/**
	 * Get the parser listener registered with this C24ItemReader (if any)
	 * @return The currently registered ParseListener, null if there isn't one.
	 */
	public ParseListener<Object, Result> getParseListener() {
		return parseListener;
	}

	/**
	 * Registers a ParseListener
	 * @param parseListener The object which should receive the callbacks, null to remove an existing ParseListener
	 */
	public void setParseListener(ParseListener<Object, Result> parseListener) {
		this.parseListener = parseListener;
	}

	/**
	 * Returns the element type that we will attempt to parse from the source
	 */
	public Element getElementType() {
		return elementType;
	}

	/**
	 * Set the type of element that we will attempt to parse from the source
	 * 
	 * @param elementType The type of element that we want to parse from the source
	 */
	public void setElementType(Element elementType) {
		this.elementType = elementType;
	}
	
	/**
	 * Allows setting of element type via the supplied model
	 * 
	 * @param model The model of the type we wish to parse
	 */
	public void setModel(C24Model model) {
		elementType = model.getRootElement();
	}
	
	/**
	 * Returns the regular expression that we're using to split up in the incoming data.
	 * Null if not set.
	 */
	public String getElementStartPattern() {
		return elementStartPattern != null? elementStartPattern.pattern() : null;
	}

	/**
	 * Sets the regular expression used to quickly split up the source into individual entities for parsing
	 * 
	 * @param elementStartRegEx The regular expression to identify the start of a new entity in the source
	 */
	public void setElementStartPattern(String elementStartRegEx) {
		this.elementStartPattern = Pattern.compile(elementStartRegEx, Pattern.DOTALL);
	}
	
	/**
	 * Returns the pattern we're using to to determine the end of a message.
	 * 
	 * @return end of element pattern. Null if not set.
	 */
	public String getElementStopPattern() {
		return elementStopPattern != null? elementStopPattern.pattern() : null;
	}

	/**
	 * In conjunction with the element start regex, used to detect the end of a message. Note that it is possible for a single
	 * line to match both the start and stop patterns and hence be a complete element on its own.
	 * 
	 * @param elementStopRegEx The regular expression to identify the end of an entity in the source
	 */
	public void setElementStopPattern(String elementStopRegEx) {
		this.elementStopPattern = Pattern.compile(elementStopRegEx, Pattern.DOTALL);
	}

	/**
	 * Set whether or not you want validation to be performed on the parsed CDOs. 
	 * An exception will be thrown for any entity which fails validation.
	 * 
	 * @param validate Whether or not to validate parsed CDOs
	 */
	public void setValidate(boolean validate) {
		validator = validate? new ThreadLocal<ValidationManager>() : null;
	}
	
	/**
	 * Query whether or not this ItemReader will validate parsed CDOs
	 * 
	 * @return True iff this ItemReader will automatically validate read CDOs
	 */
	public boolean isValidating() {
		return validator != null;
	}
	
	/**
	 * Gets the SplittingReaderSource from which CDOs are being parsed
	 * 
	 * @return This reader's SplittingReaderSource
	 */
	public SplittingReaderSource getSource() {
		return source;
	}

	/**
	 * Sets the source that this reader will read from
	 * 
	 * @param source The SplittingReaderSource to read data from
	 */
	public void setSource(SplittingReaderSource source) {
		this.source = source;
	}
	
	/**
	 * Sets the iO source factory to use
	 * 
	 * @param ioSourceFactory
	 */
	public void setSourceFactory(SourceFactory ioSourceFactory) {
		this.ioSourceFactory = ioSourceFactory;
	}
	
	public SourceFactory getSourceFactory() {
		return this.ioSourceFactory;
	}
	
	/**
	 * Initialise our context
	 * 
	 * @param stepExecution The step execution context
	 */
	@BeforeStep
	public void setup(StepExecution stepExecution) {		
		source.initialise(stepExecution);
	}
	
	/**
	 * Clean up any resources we're consuming
	 */
	@AfterStep
	public void cleanup() {
		if(validator != null) {
			validator = new ThreadLocal<ValidationManager>();
		}
		source.close();
	}
	

	/**
	 * Structure to associate a to-be-parsed element with externally supplied context.
	 * The ParseListener callback enables an external object to associate context with an element. This structure 
	 * allows them to be held together during processing; this is necessary to avoid race conditions.
	 * 
	 * @author Andrew Elmore
	 */
	protected static class ElementContext {
		public ElementContext(String element, Object context) {
			this.element = element;
			this.context = context;
		}
		public Object context;
		public String element;
	}
	
	
	/**
	 * Reads a line of text from the SplittingReader. The definition of line is implementation dependent.
	 * This implementation breaks lines around carriage returns and line feeds.
	 * 
	 * @param reader The SplittingReader to consume characters from
	 * @return A line of text
	 * @throws IOException
	 */
	protected String readLine(SplittingReader reader) throws IOException {	    
	    return reader.readLine();
	}
	
	/**
	 * Extracts the textual data for an element from the SplittingReader using the elementStartPattern to split
	 * up the data. 
	 * 
	 * If a ParseListener is registered, it will receive a callback when a line is read from the reader and when 
	 * an element has been extracted.
	 * 
	 * @param reader The SplittingReader to extract the element from
	 */
	protected ElementContext readElement(SplittingReader reader) {

		StringBuffer elementCache = new StringBuffer();
		boolean inElement = false;	
		
		synchronized(reader) {
    		try {
    			while(reader.ready()) {
    			    String line = readLine(reader);
    
    				if(line != null) {
    					if(parseListener != null) {
    						// Invoke callback
    						line = parseListener.processLine(line);
    					}
    					// We look for the start of a new element if either:
    					// a) We're not in an element or
    					// b) We don't have an elementStopPattern set (if we do and we're in a element, the presence of a line
    					// that matches the element start pattern is deemed to still be part of the same element)
    					if((!inElement || elementStopPattern == null) && elementStartPattern.matcher(line).matches()) {
    						// We've encountered the start of a new element
    						String message = elementCache.toString();
    						if(message.trim().length() > 0) {
    							// We were already parsing an element; thus we've finished extracting our element
    							// Cache the line
    						    reader.pushback(line);
    							// ...and return what we have already extracted
    							ElementContext context = new ElementContext(message, parseListener == null? null : parseListener.getContext(message));
    							return context;
    						} else {
    							// This is the start of our element. Add it to our elementCache.
    							inElement = true;
    						}
    					} 
    					
    					if(inElement) {
    						// More data for our current element
    						elementCache.append(line);
    						
    						// If we have an elementStopPattern, see if the line matched
    						if(elementStopPattern != null && elementStopPattern.matcher(line).matches()) {
    							// We've encountered the end of the element
    							break;
    						}
    					}
    				}
    			}
    		} catch(IOException ioEx) {
    			throw new NonTransientResourceException("Failed to extract entity", ioEx);
    		}
		}

		String message = elementCache.toString();
		ElementContext context = new ElementContext(message, parseListener == null? null : parseListener.getContext(message));
		return context;
	}
	
	/**
	 * Called once a thread determines it has exhausted the current parser (more accurately, the underlying Reader).
	 * Triggers creation of an appropriate new Parser next time getParser is called.
	 * 
	 * @param parser The parser that has been exhausted.
	 * @throws IOException 
	 */
	private void discardParser(Parser parser) {
		// If there's no splitting pattern, we have to ensure that we discard the underlying reader too
		if(elementStartPattern == null) {
			try {
				source.discard(parser.getSplitter());
			} catch(IOException ioEx) {
				// We'll carry on; worst case scenario a failure will be logged multiple times
				LOG.warn("Failed to close reader on source {}", source.getName());
			}
		}
		if(this.elementStartPattern == null && source.useMultipleThreadsPerReader()) {
			synchronized(this) {
				if(this.parser == parser) {
					this.parser = null;
				}
			}
		} else {
			threadedParser.set(null);
		}
	}
	
	/**
	 * Gets the appropriate iO Source to use to read the message.
	 * If ioSourceFactory is not set, it defaults to the model's default source.
	 * 
	 * @param An optional Reader to pass to the source's setReader method
	 * 
	 * @return A configured iO source
	 */
	private Source getIoSource(Reader reader) {
		Source source = null;
		
		if(ioSourceFactory == null) {
			// Use the default
			source = elementType.getModel().source();
			if(reader != null) {
				source.setReader(reader);
			}
		} else {
			// If the reader is null, we have to give the factory a dummy one
			source = ioSourceFactory.getSource(reader != null? reader : new StringReader(""));
		}
		
		if(source instanceof TextualSource) {
			((TextualSource)source).setEndOfDataRequired(false);
		}
		
		return source;
	}
	
	/**
	 * Gets a configured parser for this thread to use to parse messages.
	 * Depending on configuration, threads may or may not share the source.
	 * 
	 * @return The parser this thread should use to parse messages.
	 */
	private Parser getParser() {
		
		Parser returnParser = null;
		
		// We operate in one of 3 modes
		// 1. We have no splitter pattern and the ReaderSource advises us to share the Reader between threads
		// In this case all threads must share the same parser; make sure that we return a synchronized parser
		if(this.elementStartPattern == null && source.useMultipleThreadsPerReader()) {
			returnParser = parser;
			if(returnParser == null) {
				synchronized(this) {
					if(parser == null) {
						SplittingReader splitter = source.getReader();
						if(splitter != null) {
							returnParser = new SyncParser(splitter, getIoSource(splitter), elementType);
							parser = returnParser;							
						}
					}
				}
			}
		}
		
		// 2. The ReaderSource advises us not to share the reader between threads
		// In this case, each thread will have its own parser and we need to ask for a new Reader each time we create one
		else if(!source.useMultipleThreadsPerReader()) {
			returnParser = threadedParser.get();

			boolean needNewReader = returnParser == null;
			if(!needNewReader) {
				try {
					needNewReader = !returnParser.getReader().ready();
				} catch (IOException ex) {
					// Unhelpfully if the stream has been closed beneath our feet this is how we find out about it
					// Even more unhelpfully, it appears as though the SAXParser does exactly that when it's finished parsing
					needNewReader = true;
				}
			}
			
			if(needNewReader) {
				SplittingReader splitter = source.getNextReader();
				if(splitter != null) {
					// If we don't have a splitting pattern, pass the splitter directly to the iO source
					// If we do, pass null as we'll create a new Reader for it below
					returnParser = new Parser(splitter, getIoSource(this.elementStartPattern == null? splitter : null), elementType);					
					threadedParser.set(returnParser);
				}
			}

			
		}
		// 3. We have a splitter pattern and the Reader source advises us to share the Reader between threads
		// In this case each thread will have its own parser but we'll share a reader and keep using it until it runs out
		else {
			returnParser = threadedParser.get();
			if(returnParser == null) {
			    SplittingReader splitter = source.getReader();
				if(splitter != null) {
					returnParser = new Parser(splitter, getIoSource(null), elementType);					
					threadedParser.set(returnParser);
				}
			}			
		}
		
		return returnParser;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Result read() throws UnexpectedInputException,
			ParseException, NonTransientResourceException {
		
		ComplexDataObject result = null;
		Object context = null;
		Parser parser = null;
		
		// Keep trying to parse an entity until either we get one (result != null) or we run out of data to read (parser == null)
		// BufferedReaderSources such as the ZipFileSource can return multiple BufferedReaders; when our current one is exhausted it
		// will return another one
		while(result == null && (parser = getParser()) != null) {
			
			if(elementStartPattern != null) {
				
				// We're possibly sharing a BufferedReader with other threads. Get our data out of it as quickly as we can to reduce
				// the amount of time we spend blocking others
			    SplittingReader reader = parser.getSplitter();
                if(reader == null) {
                    // There's nothing left to read
                    break;
                }
				
				// Get the textual source for an element from the reader
				ElementContext elementContext = readElement(reader);
				String element = elementContext.element;
				context = elementContext.context;
				
				// If we got something then parse it
				if(element != null && element.trim().length() > 0) {
					
					StringReader stringReader = new StringReader(element);

					parser.setReader(stringReader);
				
					try {
						result = parser.read();
					} catch(IOException ioEx) {
						throw new ParseException("Failed to parse CDO from " + source.getName() + ". Message: " + element, ioEx);
					}
				} else {
					// This parser has been exhausted
					discardParser(parser);
				}
				
			} else {
				// We'll parse CDOs from the parser in serial
				try {
					result = parser.read();
				} catch(IOException ioEx) {
					throw new ParseException("Failed to parse CDO from " + source.getName(), ioEx);
				} finally {
					if(result != null && result.getTotalAttrCount() == 0 && result.getTotalElementCount() == 0) {
						// We didn't manage to read anything
						result = null;
					}
					if(result == null) {
						// We've exhausted this reader
						// In the event of an exception being thrown there might still be data left in the reader 
						// but as we have no way to skip to the next message, we have to abandon it
						discardParser(parser);
					}
				}
			}
		}
		
		if(validator != null && result != null) {
			try {
				ValidationManager mgr = validator.get();
				if(mgr == null) {
					mgr = new ValidationManager();
					validator.set(mgr);
				}
				mgr.validateByException(result);
			} catch(ValidationException vEx) {
				throw new C24ValidationException("Failed to validate message: " + vEx.getLocalizedMessage() + " [" + source.getName() + "]", result, vEx);
			}
		}
		
		// If we have a ParseListener registered, allow it to intercept the return value
		return parseListener == null || result == null? (Result)result : parseListener.process(result, context);
		
	}
	

}

