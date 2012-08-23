package biz.c24.io.spring.batch.reader;

import biz.c24.io.api.data.ComplexDataObject;

/**
 * Callback interface used by clients who wish to intercept the parsing process
 * 
 * @author Andrew Elmre
 *
 * @param <Context> Contextual information to be held during parsing
 * @param <Result> The type that the ItemReader will return
 */
public interface ParseListener<Context, Result> {
	
	/**
	 * Callback invoked where the ItemReader is spltting the file (ie where an elementStartPattern is specified).
	 * Allows the line to be read (e.g. to cache context information) and modified if desired.
	 * If no modifications are required, the line should be returned as passed in
	 * @param line The line of text the parser is about to process
	 * @return The line of text that the parser will receive
	 */
	public String processLine(String line);
	
	/**
	 * Allows the callback to associate some context with the element that the parser is about to parse.
	 * Only applicable where the ItemReader is spltting the file (ie where an elementStartPattern is specified).
	 * The parser makes no use of the context, however will supply it back when it calls process
	 * @param element The textual element that the parser is about to parse
	 * @return Context that the callback would like to receive when the parsed object is passed in the process method
	 */
	public Context getContext(String element); 
	
	/**
	 * Allows the callback to modify the object returned by C24ItemReader.read()
	 * This can be used, for example, to either enrich the CDO or to wrap it with an object which contains contextual 
	 * information to be passed to downstream processors.
	 * 
	 * @param obj The ComplexDataObject which has just been parsed by the ItemReader
	 * @param context The context (if any) that was returned when the ItemReader invoked getContext for the element
	 * @return The object that C24ItemReader will return from its read() method
	 */
	public Result process(ComplexDataObject obj, Context context);

}
