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

import biz.c24.io.spring.batch.writer.C24ItemWriter;
import biz.c24.io.spring.batch.writer.source.FileWriterSource;
import biz.c24.io.spring.batch.writer.source.ZipFileWriterSource;

/**
 * Parser for the 'C24ItemWriter' element.
 * 
 * @author Andrew Elmore
 */
public class ItemWriterParser extends AbstractSingleBeanDefinitionParser {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
    protected Class<?> getBeanClass(Element element) {
        return C24ItemWriter.class;
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
    	
    	// Mandatory
    	String sinkRef = element.getAttribute("sink-ref");
    	bean.addPropertyReference("sink", sinkRef);
    	
    	int numSourceDefns = 0;
    	
    	// Optional
    	String writerSourceRef = element.getAttribute("writer-source-ref");
    	if(StringUtils.hasText(writerSourceRef)) {
    	    bean.addPropertyReference("writerSource", writerSourceRef);
    	    numSourceDefns++;
    	}

        Element fileSourceElement = DomUtils.getChildElementByTagName(element, "file-writer");
        if(fileSourceElement != null) {
            BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(fileSourceElement,
                    bean.getBeanDefinition());
            beanDefinition.setBeanClassName(FileWriterSource.class.getName());
            bean.addPropertyValue("writerSource", beanDefinition);
            numSourceDefns++;
        }
        
        Element zipFileSourceElement = DomUtils.getChildElementByTagName(element, "zip-file-writer");
        if(zipFileSourceElement != null) {
            BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(zipFileSourceElement,
                    bean.getBeanDefinition());
            beanDefinition.setBeanClassName(ZipFileWriterSource.class.getName());
            bean.addPropertyValue("writerSource", beanDefinition);
            numSourceDefns++;
        }
       
        if(numSourceDefns > 1) {
            parserContext.getReaderContext().error("Only one of writer-source-ref, file-writer and zip-file-writer can be used", element);
        } else if(numSourceDefns == 0) {
            parserContext.getReaderContext().error("One of writer-source-ref, file-writer and zip-file-writer must be specified", element);            
        }
    }    
}
