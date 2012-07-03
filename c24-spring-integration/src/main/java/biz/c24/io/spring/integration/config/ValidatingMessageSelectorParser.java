package biz.c24.io.spring.integration.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

import biz.c24.io.spring.integration.selector.C24ValidatingMessageSelector;

public class ValidatingMessageSelectorParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(C24ValidatingMessageSelector.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "fail-fast", "failFast");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "throw-exception-on-rejection", "throwExceptionOnRejection");
		
		return builder.getBeanDefinition();
	}

}
