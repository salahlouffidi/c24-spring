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
import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * ItemReader that reads ComplexDataObjects from an XML-based BufferedReaderSource.
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
public class C24XmlItemReader<Result> extends C24ItemReader<Result> {
	
	private static Logger LOG = LoggerFactory.getLogger(C24XmlItemReader.class);

	/**
	 * Reads 'lines' of text from an XML file.
	 * Lines are broken around the start of each new element
	 * NB does not yet support '<' being used outside of an element declaration (e.g. within a CDATA section)
	 */
	@Override
    protected String readLine(BufferedReader reader) throws IOException {

        StringBuffer line = new StringBuffer();
        char c = 0;
        while(reader.ready() && ((c = (char) reader.read()) != '<' || line.length() == 0)  ) {
            if(line.length() == 0 && c != '<') {
                // Our previous read over-read by a character. Unfortunately we can't rewind as our parent
                // maintains a mark for its own purposes
                line.append('<');
            }
            line.append(c);
        }
        
        return line.toString();
    }	

}

