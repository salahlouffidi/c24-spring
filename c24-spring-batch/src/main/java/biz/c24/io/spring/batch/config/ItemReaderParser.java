/*
 * Copyright 2012 C24 Technologies.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.spring.batch.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import biz.c24.io.spring.batch.reader.C24ItemReader;
import biz.c24.io.spring.batch.reader.source.FileSource;
import biz.c24.io.spring.batch.reader.source.ZipFileSource;

/**
 * Parser for the 'C24ItemReader' element.
 * 
 * @author Andrew Elmore
 */
public class ItemReaderParser extends AbstractSingleBeanDefinitionParser {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
    protected Class<?> getBeanClass(Element element) {
        return C24ItemReader.class;
    }

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder bean) {

    	// Optional
    	String scope = element.getAttribute("scope");
    	if(StringUtils.hasText(scope)) {
    		bean.setScope(scope);
    	} else {
    		// Default to step scope
    		bean.setScope("step");
    	}
    	
    	int numSourceDefns = 0;
    	
    	// Optional
    	String sourceRef = element.getAttribute("source-ref");
    	if(StringUtils.hasText(sourceRef)) {
    	    bean.addPropertyReference("source", sourceRef);
    	    numSourceDefns++;
    	}
    	
    	// Mandatory
    	String modelRef = element.getAttribute("model-ref");
    	bean.addPropertyReference("model", modelRef);
    	
    	// Optional
    	String elementStartPattern = element.getAttribute("elementStartPattern");
    	if(StringUtils.hasText(elementStartPattern)) {
    		bean.addPropertyValue("elementStartPattern", elementStartPattern);
    	}
    	
    	// Optional
    	String elementStopPattern = element.getAttribute("elementStopPattern");
    	if(StringUtils.hasText(elementStopPattern)) {
    		bean.addPropertyValue("elementStopPattern", elementStopPattern);
    	}
    	
    	// Optional
    	String validate = element.getAttribute("validate");
    	if(StringUtils.hasText(validate)) {
    		boolean val = Boolean.parseBoolean(validate);
    		bean.addPropertyValue("validate", val);
    	}
    	
    	// Optional
    	String failfast = element.getAttribute("failfast");
    	if(StringUtils.hasText(failfast)) {
    		boolean val = Boolean.parseBoolean(failfast);
    		bean.addPropertyValue("failfast", val);
    	}
    	
    	// Optional
    	String sourceFactoryRef = element.getAttribute("source-factory-ref");
    	if(StringUtils.hasText(sourceFactoryRef)) {
    		bean.addPropertyReference("sourceFactory", sourceFactoryRef);
    	}
    	
    	// Optional
    	String parseListenerRef = element.getAttribute("parse-listener-ref");
    	if(StringUtils.hasText(parseListenerRef)) {
    		bean.addPropertyReference("parseListener", parseListenerRef);
    	}
    	
    	   	
    	Element fileSourceElement = DomUtils.getChildElementByTagName(element, "file-source");
    	if(fileSourceElement != null) {
            BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(fileSourceElement,
                    bean.getBeanDefinition());
            beanDefinition.setBeanClassName(FileSource.class.getName());
            bean.addPropertyValue("source", beanDefinition);
            numSourceDefns++;
    	}
    	
        Element zipFileSourceElement = DomUtils.getChildElementByTagName(element, "zip-file-source");
        if(zipFileSourceElement != null) {
            BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(zipFileSourceElement,
                    bean.getBeanDefinition());
            beanDefinition.setBeanClassName(ZipFileSource.class.getName());
            bean.addPropertyValue("source", beanDefinition);
            numSourceDefns++;
        }
       
        if(numSourceDefns > 1) {
            parserContext.getReaderContext().error("Only one of source-ref, file-source and zip-file-source can be used", element);
        } else if(numSourceDefns == 0) {
            parserContext.getReaderContext().error("One of source-ref, file-source and zip-file-source must be specified", element);            
        }
        
    }    
}
