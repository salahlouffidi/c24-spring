package biz.c24.io.spring.source;

import biz.c24.io.api.presentation.JsonSourcev2;
import biz.c24.io.api.presentation.Source;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by iainporter on 21/07/2014.
 */
public class JsonSourceFactory implements SourceFactory  {

    private String encoding;

    @Override
    public Source getSource(Reader reader) {
        JsonSourcev2 source = new JsonSourcev2(reader);
        configure(source);
        return source;
    }

    @Override
    public Source getSource(InputStream stream) {
        JsonSourcev2 source = new JsonSourcev2(stream);
        configure(source);
        return source;
    }

    @Override
    public Source getSource() {
        JsonSourcev2 source = new JsonSourcev2();
        configure(source);
        return source;
    }

    final protected void configure(JsonSourcev2 source) {
        if(encoding != null) {
            source.setEncoding(encoding);
        }
    }

    /**
     * Override this to provide configuration to the source
     *
     * @param jsonSource
     */
    protected void doConfigure(JsonSourcev2 jsonSource) {

    }
}
