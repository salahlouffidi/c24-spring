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

/**
 * Mock Reader class that allows the supplied String to be read from it for a given number of times,
 * after which subsequent calls to read return EOF.
 * 
 * Not synchronised as for performance it is more efficient to handle this at a higher level.
 * 
 * @author Andrew Elmore
 *
 */
public class MockReader extends Reader {
    
    private final char[] buffer;
    private final int bufferLength;
    
    private int index = 0;
    private int timesRead = 0;
    private final int maxTimesRead;
    
    public MockReader(String data, int maxTimesRead) {
        this.buffer = data.toCharArray();
        this.bufferLength = buffer.length;
        this.maxTimesRead = maxTimesRead;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = 0;
        while(charsRead < len && timesRead < maxTimesRead) {

            int charsToCopy = bufferLength - index;
            if(len < charsToCopy) {
                charsToCopy = len;
            }
            System.arraycopy(buffer, index, cbuf, off, charsToCopy);
            
            index += charsToCopy;
            off += charsToCopy;
            charsRead += charsToCopy;
            len -= charsToCopy;

            if(index == bufferLength) {
                timesRead++;
                index = 0;
            }
        }
        return charsRead == 0? -1 : charsRead;
    }

    @Override
    public void close() throws IOException {
        
    }
    
    @Override
    public boolean ready() {
        return timesRead < maxTimesRead;
    }

}
