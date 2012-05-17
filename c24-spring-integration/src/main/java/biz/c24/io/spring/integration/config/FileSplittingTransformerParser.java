package biz.c24.io.spring.integration.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractTransformerParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

public class FileSplittingTransformerParser extends AbstractTransformerParser {

    @Override
    protected String getTransformerClassName() {
        return "biz.c24.io.spring.integration.transformer.C24FileSplittingTransformer";
    }

    @Override
    public void parseTransformer(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        String messageProcessingChannel = element.getAttribute("message-processing-channel");
		Assert.hasText(messageProcessingChannel, "The 'message-processing-channel' attribute is required.");
		builder.addConstructorArgReference(messageProcessingChannel);

        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "batch-size", "batchSize");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "initiator", "initiator");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "terminator", "terminator");
    }
}