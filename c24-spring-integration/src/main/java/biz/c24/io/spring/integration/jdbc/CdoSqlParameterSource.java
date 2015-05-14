package biz.c24.io.spring.integration.jdbc;


import biz.c24.io.api.data.ComplexDataObject;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;

/**
 * Implementation of {@Link org.springframework.jdbc.core.namedparam.SqlParameterSource} that extracts parameter values
 * from an instance of a {@Link biz.c24.io.api.data.ComplexDataObject}
 *
 * @author Iain Porter
 * @since 3.0.6
 */
public class CdoSqlParameterSource extends AbstractSqlParameterSource {

    private final ComplexDataObject complexDataObject;

    public CdoSqlParameterSource(final ComplexDataObject cdo) {
        complexDataObject = cdo;
    }

    @Override
    public boolean hasValue(final String paramName) {
        return complexDataObject.containsElementDecl(paramName);
    }

    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        return complexDataObject.getElement(paramName);
    }
}
