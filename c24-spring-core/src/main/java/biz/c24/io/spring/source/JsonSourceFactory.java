package biz.c24.io.spring.source;

import biz.c24.io.api.presentation.JsonSource;
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
        JsonSource source = new JsonSource(reader);
        configure(source);
        return source;
    }

    @Override
    public Source getSource(InputStream stream) {
        JsonSource source = new JsonSource(stream);
        configure(source);
        return source;
    }

    @Override
    public Source getSource() {
        JsonSource source = new JsonSource();
        configure(source);
        return source;
    }

    final protected void configure(JsonSource source) {
        if(encoding != null) {
            source.setEncoding(encoding);
        }
    }

    /**
     * Override this to provide configuration to the source
     *
     * @param jsonSource
     */
    protected void doConfigure(JsonSource jsonSource) {

    }
}
