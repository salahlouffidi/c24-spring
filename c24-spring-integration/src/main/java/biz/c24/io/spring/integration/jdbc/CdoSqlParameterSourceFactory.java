package biz.c24.io.spring.integration.jdbc;


import biz.c24.io.api.data.ComplexDataObject;
import org.springframework.integration.jdbc.SqlParameterSourceFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.integration.jdbc.SqlParameterSourceFactory} for creating {@link biz.c24.io.spring.integration.jdbc.CdoSqlParameterSource}
 * to reference elements in a {@link biz.c24.io.api.data.ComplexDataObject} extracted from a {@link org.springframework.messaging.Message} payload
 *
 * @author Iain Porter
 * @since 3.0.6
 */
public class CdoSqlParameterSourceFactory implements SqlParameterSourceFactory {

    @Override
    public SqlParameterSource createParameterSource(Object input) {
        Assert.notNull(input, "Input to Source Factory must not be null");
        Assert.isInstanceOf(Message.class, input, "input must be instance of org.springframework.messaging.Message");
        Message message = (Message) input;
        Assert.isInstanceOf(ComplexDataObject.class, message.getPayload(), "Payload of the message must be a biz.c24.io.api.data.ComplexDataObject");
        ComplexDataObject payload = (ComplexDataObject)message.getPayload();
        return new CdoSqlParameterSource(payload);
    }
}
