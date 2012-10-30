/*
 * Copyright 2012 C24 Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.spring.batch.reader.performance.util;

import java.io.IOException;
import java.io.Reader;

import org.springframework.batch.core.StepExecution;

import biz.c24.io.spring.batch.reader.source.SplittingReader;
import biz.c24.io.spring.batch.reader.source.SplittingReaderSource;

/**
 * Mock SplittingReaderSource that returns the supplied Reader until it is exhausted
 * 
 * @author Andrew Elmore
 *
 */
public class MockSplittingReaderSource implements SplittingReaderSource {
    
    private SplittingReader reader;
    private final boolean useMultipleThreadsPerReader;
    
    public MockSplittingReaderSource(Reader reader, boolean consistentLineEndings, boolean useMultipleThreadsPerReader) {
        this.reader = new SplittingReader(reader, consistentLineEndings);
        this.useMultipleThreadsPerReader = useMultipleThreadsPerReader;
    }
    
    @Override
    public String getName() {
        return "MockSplittingReaderSource";
        
    }

    @Override
    public void initialise(StepExecution stepExecution) {
        
    }

    @Override
    public void close() {
        
    }

    @Override
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
        return useMultipleThreadsPerReader;
    }

    @Override
    public void discard(SplittingReader reader) throws IOException {
        reader = null;
    }

}
