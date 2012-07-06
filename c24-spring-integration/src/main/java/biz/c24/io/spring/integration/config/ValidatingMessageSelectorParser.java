/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.spring.integration.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

import biz.c24.io.spring.integration.selector.C24ValidatingMessageSelector;

/**
 * Parser for the 'validating-selector' element
 * 
 * @author Andrew Elmore
 *
 */
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
