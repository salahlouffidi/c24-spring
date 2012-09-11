package biz.c24.io.spring.batch.config;

import org.w3c.dom.Element;

import biz.c24.io.spring.batch.reader.source.ZipFileSource;

public class ZipFileSourceParser extends FileSourceParser {
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ZipFileSource.class;
    }
}
