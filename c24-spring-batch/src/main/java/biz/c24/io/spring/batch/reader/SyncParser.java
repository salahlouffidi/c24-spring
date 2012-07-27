package biz.c24.io.spring.batch.reader;

import java.io.IOException;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.presentation.Source;

/**
 * A synchronised parser; to be used where the parser is shared between multiple threads
 * 
 * @author andrew
 *
 */
public class SyncParser extends Parser {
	
	public SyncParser(Source ioSource, Element element) {
		super(ioSource, element);
	}
	
	public synchronized ComplexDataObject read() throws IOException {
		return super.read();
	}

}
