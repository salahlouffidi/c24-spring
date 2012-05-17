package biz.c24.io.spring.integration.transformer;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class C24FileSplittingTransformer extends AbstractPayloadTransformer<Object, Object> {

    private final static int DEFAULT_BATCH_SIZE = 1;
    private final static String DEFAULT_TERMINATOR = System.getProperty("line.separator");
    private Pattern initiator;
    private String terminator = DEFAULT_TERMINATOR;
    private final MessageChannel messageProcessingChannel;
    private int batchSize = DEFAULT_BATCH_SIZE;


    public C24FileSplittingTransformer(MessageChannel messageProcessingChannel) {
          this.messageProcessingChannel = messageProcessingChannel;
    }

    public void setInitiator(String initiatorRegEx) {
        this.initiator = Pattern.compile(initiatorRegEx);
    }



    @Override
    protected Object transformPayload(Object payload) throws Exception {
        File file = (File)payload;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            StringBuffer currentMessage = null;
            int linesProcessed = 0;
            List<String> batchContainer = new ArrayList<String>(batchSize);
            // Read through the file
            while (reader.ready()) {
                String line = reader.readLine();
                if (initiator == null || initiator.matcher(line).matches()) {
                    // We've encountered a line which matches our message start pattern
                    // Dump out the current message
                    if (currentMessage != null) {
                        linesProcessed += 1;
                        batchContainer.add(currentMessage.toString());
                        if ((linesProcessed % batchSize) == 0) {
                            messageProcessingChannel.send(MessageBuilder.withPayload(new ArrayList<String>(batchContainer)).build());
                            batchContainer.clear();
                        }
                    }
                    // Reset the StringBuffer ready for the next message
                    currentMessage = new StringBuffer();
                }
                if (currentMessage != null) {
                    currentMessage.append(line);
                    //currentMessage.append(terminator);
                }
            }

            // Ensure we write out the last message
            if(currentMessage != null && currentMessage.length() > 0) {
                batchContainer.add(currentMessage.toString());
                messageProcessingChannel.send(MessageBuilder.withPayload(new ArrayList<String>(batchContainer)).build());
                batchContainer.clear();
            }

            return Boolean.TRUE;

        } catch (FileNotFoundException fnfEx) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), fnfEx);
        } catch(IOException iEx) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), iEx);
        }
    }

    public String getTerminator() {
        return terminator;
    }

    public void setTerminator(String lineTerminator) {
        this.terminator = lineTerminator;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}