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
    private static final int DEFAULT_LINE_COUNT = 1;
    public int lineCount = 0;

    public C24FileSplittingTransformer(MessageChannel messageProcessingChannel) {
        this.messageProcessingChannel = messageProcessingChannel;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    @Override
    protected Object transformPayload(Object payload) throws Exception {

        lineCount = (lineCount == 0 ? DEFAULT_LINE_COUNT : lineCount);
        File file = (File) payload;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int linesProcessed = 0;
            List<String> lineContainer = new ArrayList<String>(lineCount);
            while (reader.ready()) {

                lineContainer.add(reader.readLine());
                linesProcessed += 1;

                if ((linesProcessed % lineCount) == 0) {
                    messageProcessingChannel.send(MessageBuilder.withPayload(new ArrayList<String>(lineContainer)).build());
                    lineContainer.clear();
                }
            }
            return new Boolean(true);
        } catch (FileNotFoundException fnfEx) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), fnfEx);
        } catch (IOException iEx) {
            throw new MessageHandlingException(MessageBuilder.withPayload(payload).build(), iEx);
        }
    }

}