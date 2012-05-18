package biz.c24.io.spring.batch.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import biz.c24.io.spring.batch.processor.C24TransformItemProcessor;

public class TransformItemProcessorParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return C24TransformItemProcessor.class;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		// Mandatory
		String transformId = element.getAttribute("transform-ref");
		builder.addPropertyReference("transformer", transformId);
		
		// Optional
		String validate = element.getAttribute("validate");
		if(StringUtils.hasText(validate)) {
			boolean val = Boolean.parseBoolean(validate);
			builder.addPropertyValue("validation", val);
		}
	}

}
