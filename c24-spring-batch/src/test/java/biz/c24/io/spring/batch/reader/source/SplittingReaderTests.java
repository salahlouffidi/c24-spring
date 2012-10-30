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
package biz.c24.io.spring.batch.reader.source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Validate the functionally correct behaviour of the SplittingReader
 *
 * @author Andrew Elmore
 *
 */
public class SplittingReaderTests {

    @Test
    public void testLineSplit() throws IOException {
        String testString = "String 1\nString 2\r\nString 3\rString 4";
        SplittingReader reader = new SplittingReader(new StringReader(testString), false);
        
        assertThat(reader.readLine(), is("String 1\n"));
        assertThat(reader.readLine(), is("String 2\r\n"));
        assertThat(reader.readLine(), is("String 3\r"));
        assertThat(reader.readLine(), is("String 4"));
        assertThat(reader.readLine(), is(nullValue()));
        
    }
    
    @Test 
    public void testLinePushback() throws IOException {
        //  String testString = "Test string containing multiple repeated characters for split test purposes\nAlso contains a number of different line endings\r\n" +
        //             "To ensure that the parser\r"
          String testString = "String 1\nString 2\r\n";
          SplittingReader reader = new SplittingReader(new StringReader(testString));
          
          String line = reader.readLine();
          assertThat(line, is("String 1\n"));
          reader.pushback(line);
          assertThat(reader.readLine(), is("String 1\n"));
          assertThat(reader.readLine(), is("String 2\r\n"));
          assertThat(reader.readLine(), is(nullValue()));
          
      }    
    
    @Test
    public void readUntil() throws IOException {
        String testString = "String 1\nString 2\r\nString 3\rString 4";
        SplittingReader reader = new SplittingReader(new StringReader(testString));
        
        assertThat(reader.readUntil('i'), is("Str"));
        assertThat(reader.readUntil('i'), is("ing 1\nStr"));
        assertThat(reader.readUntil('i'), is("ing 2\r\nStr"));
        assertThat(reader.readUntil('i'), is("ing 3\rStr"));
        assertThat(reader.readUntil('i'), is("ing 4"));
        assertThat(reader.readUntil('i'), is(nullValue()));        
    }
    
    @Test
    public void readUntilPushback() throws IOException {
        String testString = "String 1\nString 2\r\nString 3\rString 4";
        SplittingReader reader = new SplittingReader(new StringReader(testString));
        
        assertThat(reader.readUntil('i'), is("Str"));
        String line = reader.readUntil('i');
        assertThat(line, is("ing 1\nStr"));
        reader.pushback(line);
        // Note we expect this to return line no matter what value is passed in
        assertThat(reader.readUntil('X'), is("ing 1\nStr"));        
        assertThat(reader.readUntil('i'), is("ing 2\r\nStr"));
        assertThat(reader.readUntil('i'), is("ing 3\rStr"));
        assertThat(reader.readUntil('i'), is("ing 4"));
        assertThat(reader.readUntil('i'), is(nullValue()));        
    }
    
    @Test
    public void testArrayRead() throws IOException {
        String testString = "String 1\nString 2\r\nString 3\rString 4";
        SplittingReader reader = new SplittingReader(new StringReader(testString));
        
        char[] cbuf = new char[100];
        
        int charsRead = reader.read(cbuf, 0, 10);
        assertThat(charsRead, is(10));
        assertThat(new String(cbuf,0, 10), is("String 1\nS"));
        
        charsRead = reader.read(cbuf, 10, 20);
        assertThat(charsRead, is(20));
        assertThat(new String(cbuf, 0, 30), is("String 1\nString 2\r\nString 3\rSt"));
        
        charsRead = reader.read(cbuf, 30, 100);
        assertThat(charsRead, is(6));
        assertThat(new String(cbuf, 0, 36), is("String 1\nString 2\r\nString 3\rString 4"));   
        
        charsRead = reader.read(cbuf, 36, 100);
        assertThat(charsRead, is(-1));
        assertThat(new String(cbuf, 0, 36), is("String 1\nString 2\r\nString 3\rString 4"));         
        
    }
    
    @Test
    public void testArrayPushbackRead() throws IOException {
        String testString = "String 1\nString 2\r\nString 3\rString 4";
        SplittingReader reader = new SplittingReader(new StringReader(testString));
        
        char[] cbuf = new char[100];
        
        int charsRead = reader.read(cbuf, 0, 10);
        assertThat(charsRead, is(10));
        assertThat(new String(cbuf,0, 10), is("String 1\nS"));
        
        reader.pushback("EXTRA");
        charsRead = reader.read(cbuf, 10, 3);
        assertThat(charsRead, is(3));
        assertThat(new String(cbuf, 0, 13), is("String 1\nSEXT"));
        
        charsRead = reader.read(cbuf, 13, 22);
        assertThat(charsRead, is(22));
        assertThat(new String(cbuf, 0, 35), is("String 1\nSEXTRAtring 2\r\nString 3\rSt"));
        
        charsRead = reader.read(cbuf, 35, 100);
        assertThat(charsRead, is(6));
        assertThat(new String(cbuf, 0, 41), is("String 1\nSEXTRAtring 2\r\nString 3\rString 4"));   
        
        charsRead = reader.read(cbuf, 36, 100);
        assertThat(charsRead, is(-1));
        assertThat(new String(cbuf, 0, 41), is("String 1\nSEXTRAtring 2\r\nString 3\rString 4"));         
        
    }
 

    /* Simple tests used to validate performance of the SplittingReader */

    @Test
    public void testPerfBufferedReader() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/tmp/large.xml"));
        long startTime = System.currentTimeMillis();
        String line = null;
        while((line = reader.readLine()) != null) {
            if(line.length() > 9999) {
                System.out.println("");
            }
        }
        System.out.println("Buffered Time: " + (System.currentTimeMillis() - startTime));
        
    } 
    
    @Test
    public void testPerfLineSplittingReader() throws IOException {
        SplittingReader reader = new SplittingReader(new FileReader("/tmp/large.xml"), true);
        long startTime = System.currentTimeMillis();
        String line = null;
        while((line = reader.readLine()) != null) {
            if(line.length() > 9999) {
                System.out.println("");
            }
        }
        System.out.println("Splitting Time: " + (System.currentTimeMillis() - startTime));
        
    }     
    
    @Test
    public void testPerfSplittingReader() throws IOException {
        SplittingReader reader = new SplittingReader(new FileReader("/tmp/large.xml"), true);
        long startTime = System.currentTimeMillis();
        String line = null;
        while((line = reader.readUntil('\n')) != null) {
            if(line.length() > 9999) {
                System.out.println("");
            }
        }
        System.out.println("Splitting Time: " + (System.currentTimeMillis() - startTime));
        
    }
    
    
    

    

    


}
