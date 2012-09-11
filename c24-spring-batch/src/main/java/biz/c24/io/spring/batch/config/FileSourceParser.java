package biz.c24.io.spring.batch.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import biz.c24.io.spring.batch.reader.source.FileSource;

public class FileSourceParser extends AbstractSingleBeanDefinitionParser {

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return FileSource.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
     */
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
    
        // Optional
        String resource = element.getAttribute("resource");
        if(StringUtils.hasText(resource)) {
            bean.addPropertyValue("resource", resource);            
        }
        
        // Optional
        String skipLines = element.getAttribute("skip-lines");
        if(StringUtils.hasText(skipLines)) {
            int numLines = Integer.parseInt(skipLines);
            bean.addPropertyValue("skipLines", numLines);
        }
    }
}
