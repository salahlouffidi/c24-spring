package biz.c24.io.spring.batch.reader.source;

import java.io.IOException;
import java.io.Reader;

/**
 * Utility class to rapidly split up data into lines.
 * Created instead of using a BufferedReader or Scanner as we need the ability to 'push back' at most 1 line of data while also 
 * splitting lines on an arbitrary character
 * 
 * General performance of readLine is on a par with BufferedReader; however the optimise checks add around 1% in the worst case.
 * In the best case (where only a '\r' or '\n' is used as the line terminator or readUntil is used) performance is 15-20% faster 
 * than BufferedReader.
 * 
 * @author Andrew Elmore
 *
 */
public class SplittingReader extends Reader {
    
    /**
     * Where we actually get our source data from
     */
    private Reader sourceReader;
    
    /**
     * Cache for data read from the sourceReader
     */
    private char[] buffer = new char[10000];
    /**
     * Current index into the buffer
     */
    private int index = 0;
    /**
     * Index in the buffer up to which data is populated
     */
    private int endIndex = 0;
    
    /**
     * Tracks whether we've been closed or not
     */
    boolean isOpen = true;
    
    /**
     * Allow up to one 'line' of data to be pushed back. Will be returned by any calls to readLine/Until prior to consuming more
     * data from the buffer.
     */
    private String cached = null;
    
    /**
     * If we detect a single-character line terminator, can we assume that all lines use that terminator?
     */
    private final boolean consistentLineTerminators;
    
    /**
     * Single character line terminator if detected
     */
    private Character terminator = null;
    
    public SplittingReader(Reader reader) {
        this.sourceReader = reader;
        this.consistentLineTerminators = false;
    }
    
    /**
     * 
     * @param reader The underlying Reader to extract data from
     * @param consistentLineTerminators Set to true if all lines use the same line terminator for an approx 15% speed boost
     */
    public SplittingReader(Reader reader, boolean consistentLineTerminators) {
        this.sourceReader = reader;
        this.consistentLineTerminators = consistentLineTerminators;
    }
    
    public Reader getReader() {
        return sourceReader;
    }
    
    /**
     * Overwrites the contents of the current buffer with more data if available
     * 
     * @return True iff we read more data from the underlying sourceReader
     * @throws IOException
     */
    private boolean fillBuffer() throws IOException {
        if(endIndex >= 0) {
            endIndex = sourceReader.read(buffer, 0, buffer.length);
        }
        return endIndex > 0;
    }
    
    /**
     * Extracts characters from the data stream until either:
     * a) we run out of characters to read or
     * b) the next character to be read matches c
     * 
     * In other words c is not included at the end of the stream but will be the first character
     * of the next String read via this method.
     * 
     * 
     * @param c The character to stop extracting on. 
     * @return The extracted string
     * @throws IOException
     */
    public String readUntil(char c) throws IOException {
        String result = null;
        
        if(cached != null) {
            result = cached;
            cached = null;
        } else {
            boolean parsing = true;

            while(parsing) {
                // Skip the first character - if it matches c, we want the next one anyway
                int i = result == null? index + 1 : index;
                // As odd as this construction looks, we get approx 6% speed increase over a straight while loop and updating the member var in place
                for(; i < endIndex; i++) {
                    if(buffer[i] == c) {
                        parsing = false;
                        break;
                    }
                }
                
                if(i > index && i <= endIndex) {
                    // Cache what we have so far
                    String fragment = new String(buffer, index, i - index);
                    result = result == null? fragment : result + fragment;
                }
                
                if(parsing) {
                    // We're here because we ran out of data. See if there's any more
                    if(fillBuffer()) {
                        index = 0;
                    } else {
                        parsing = false;
                    }
                } else {
                    index = i;
                }
            }
        }
        return result;
    }
    
    
    /**
     * Extracts characters from the data stream until either:
     * a) we run out of characters to read or
     * b) the last character matches c
     * 
     * Unlike readUntil, c will be included as the last character of the returned string.
     * Subsequent calls with start with the next character.
     * 
     * @param c The character to stop extracting on. 
     * @return The extracted String
     * @throws IOException
     */
    public String readUntilInclusive(char c) throws IOException {
        String result = null;
        
        if(cached != null) {
            result = cached;
            cached = null;
        } else {
            boolean parsing = true;

            while(parsing) {
                int i = index;
                // As odd as this construction looks, we get approx 6% speed increase over a straight while loop and updating the member var in place
                for(; i < endIndex; i++) {
                    if(buffer[i] == c) {
                        parsing = false;
                        i++;
                        break;
                    }
                }
                
                if(i > index && i <= endIndex) {
                    // Cache what we have so far
                    String fragment = new String(buffer, index, i - index);
                    result = result == null? fragment : result + fragment;
                }
                
                if(parsing) {
                    // We're here because we ran out of data. See if there's any more
                    if(fillBuffer()) {
                        index = 0;
                    } else {
                        parsing = false;
                    }
                } else {
                    index = i;
                }
            }
        }
        return result;
    }
    
    /**
     * Reads a line from the underlying data stream. A line is terminated with one of:
     * \n
     * \r
     * \r\n
     * 
     * If optimise is true and we notice that a single-character line terminator is being used,
     * subsequent calls will delegate to readInclusiveUntil which is slightly faster.
     * 
     * @return The extracted String
     * @throws IOException
     */
    public String readLine() throws IOException {
        String result = null;
        
        if(consistentLineTerminators && terminator != null) {
            return readUntilInclusive(terminator);
        } else if(cached != null) {
            result = cached;
            cached = null;
        } else {
            boolean parsing = true;
            char last = 'a';

            while(parsing) {
                int i = index;
                // As odd as this construction looks, we get approx 6% speed increase over a straight while loop and updating the member var in place
                for(; i < endIndex; i++) {
                    char c = buffer[i];
                    // We detect the following line terminators:
                    // \r
                    // \n
                    // \r\n
                    if(c == '\n') {
                        i++;
                        parsing = false;
                        if(consistentLineTerminators && last != '\r') {
                            terminator = '\n';
                        }
                        break;
                    } else if(last == '\r') {
                        parsing = false;
                        if(consistentLineTerminators && c != '\n') {
                            terminator = '\r';
                        }
                        break;
                    }
                    last = c;
                }
                
                if(i > index && i <= endIndex) {
                    // Cache what we have so far
                    String fragment = new String(buffer, index, i - index);
                    result = result == null? fragment : result + fragment;
                }
                
                if(parsing) {
                    // We're here because we ran out of data. See if there's any more
                    if(fillBuffer()) {
                        index = 0;
                    } else {
                        parsing = false;
                    }
                } else {
                    index = i;
                }
            }
        }
        return result;
    }        
    
    /**
     * Allow a caller to hand back a line of input to us. Subsequent attempts to read data will consume
     * from this data first.
     * 
     * @param line
     */
    public void pushback(String line) {
        cached = line;
    }
    
    /**
     * Whether or not this Reader has more data available
     */
    @Override
    public boolean ready() throws IOException {
        return cached != null || index < endIndex || sourceReader.ready();
    }
    
    /**
     * Closes this reader.
     * Implemented purely for those Reader clients which expect to get an IOException from read() once the stream is closed,
     * rather than inferring it from the return value.
     * 
     */
    @Override
    public void close() throws IOException {
        isOpen = false;
        sourceReader.close();
    }

    /*
     * (non-Javadoc)
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        
        if(!isOpen) {
            throw new IOException("Stream closed");
        }

        int startOffset = off;
        
        while(len > 0) {
            if(cached != null) {
                // Use this up first
                char[] str = cached.toCharArray();
                
                int charsToCopy = str.length;
                if(len < charsToCopy) {
                    charsToCopy = len;
                }
                System.arraycopy(str, 0, cbuf, off, charsToCopy);
                
                off += charsToCopy;
                len -= charsToCopy;
                
                if(charsToCopy < str.length) {
                    cached = new String(str, charsToCopy, str.length - charsToCopy);
                } else {
                    cached = null;
                }
            }
            
            int charsToCopy = endIndex - index;
            if(charsToCopy > 0) {
                if(len < charsToCopy) {
                    charsToCopy = len;
                }
                System.arraycopy(buffer, index, cbuf, off, charsToCopy);
                
                index += charsToCopy;
                off += charsToCopy;
                len -= charsToCopy;
            }
            
            if(len > 0) {
                // We've exhausted our buffered data - get more
                if(fillBuffer()) {
                    index = 0;
                } else {
                    break;
                }
            }
            
        }

        return startOffset == off? -1 : off - startOffset;
    } 
}
