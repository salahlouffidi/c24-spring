package biz.c24.io.spring.batch.config;

import org.w3c.dom.Element;

import biz.c24.io.spring.batch.reader.C24XmlItemReader;

public class XmlItemReaderParser extends ItemReaderParser {

    /*
     * (non-Javadoc)
     * @see biz.c24.io.spring.batch.config.ItemReaderParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return C24XmlItemReader.class;
    }
    
}
