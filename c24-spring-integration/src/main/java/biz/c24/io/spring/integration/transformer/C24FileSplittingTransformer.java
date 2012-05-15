package biz.c24.io.spring.integration.transformer;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class C24FileSplittingTransformer extends AbstractPayloadTransformer<Object, Object> {

    private final MessageChannel messageProcessingChannel;
    private static final int DEFAULT_RECORD_COUNT = 1;
    private int recordCount = 0;
    private String initiator;
    private String terminator;

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

    public C24FileSplittingTransformer(MessageChannel messageProcessingChannel) {
        this.messageProcessingChannel = messageProcessingChannel;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    private boolean isInitiatorSet() {
        return initiator == null ? false : true;
    }

    private boolean isTerminatorSet() {
        return terminator == null ? false : true;
    }

    @Override
    protected Object transformPayload(Object payload) throws Exception {

        recordCount = (recordCount == 0 ? DEFAULT_RECORD_COUNT : recordCount);
        File file = (File) payload;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int linesProcessed = 0;
            List<String> lineContainer = new ArrayList<String>(recordCount);
            while (reader.ready()) {

                lineContainer.add(reader.readLine());
                linesProcessed += 1;

                if ((linesProcessed % recordCount) == 0) {
                    messageProcessingChannel.send(MessageBuilder.withPayload(new ArrayList<String>(lineContainer)).build());
                    lineContainer.clear();
                }
            }
            return new Boolean(true);
        } catch (FileNotFoundException e) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), e);
        } catch (IOException e) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), e);
        }
    }

}