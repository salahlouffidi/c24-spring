/*
 * Copyright 2011-2012 the original author or authors.
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
package biz.c24.io.spring.integration.transformer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.Message;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.util.Assert;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.JavaClassSink;
import biz.c24.io.api.transform.Transform;

/**
 * TODO Consider adding a subclass that uses a pool of transform objects.
 * 
 * @author askogman
 * @author Andrew Elmore
 * 
 */
public class C24Transformer extends AbstractTransformer implements
InitializingBean {

	private final Log logger = LogFactory.getLog(this.getClass());

	private Class<? extends Transform> transformClass;
	private JavaClassSink javaSink = null;

	private boolean alwaysReturnArray = false;

	public Class<? extends Transform> getTransformClass() {
		return transformClass;
	}
	
    // Cache Transforms per-thread for performance
    private ThreadLocal<Transform> transform = new ThreadLocal<Transform>();

	public void setTransformClass(Class<? extends Transform> transformClass) {

		Assert.notNull(transformClass,
				"The transform class cannot be set to null");

		this.transformClass = transformClass;
	}
	
	/*
	 * Controls whether this transformer returns its default ComplexDataObject-based destination type, or instances of the supplied bean-compliant type.
	 * 
	 * @param targetClass Either null (to use the default CDO-based type) or a Java Bean compliant class whose fields match those of the target model.
	 */
	public void setTargetClass(Class<?> targetClass) {
		if(targetClass != null) {
			javaSink = new JavaClassSink();
			javaSink.setRootClass(targetClass);
		} else {
			javaSink = null;
		}
	}

	/**
	 * 
	 */
	public C24Transformer() {
	}

	/**
	 * 
	 * @return The transformed payload
	 * 
	 * @see org.springframework.integration.transformer.AbstractTransformer#doTransform
	 *      (org.springframework.messaging.Message)
	 */
	@Override
	protected Object doTransform(Message<?> message) throws Exception {

		Transform transform = buildTransform(message);

		// TODO Support list or array as input
		Object payload = message.getPayload();

		Object[][] results = transform
				.transform(new Object[][] { new Object[] { payload } });

		Object output = extractOutputPayload(results);

		return output;
	}

	protected Object extractOutputPayload(Object[][] results) throws IOException {

		if (results.length == 0) {
			// Empty matrix
			return alwaysReturnArray ? new Object[0] : null;
		}

		// Get the first vector from matrix
		Object[] resultVector = results[0];

		if (resultVector.length == 0) {
			// Empty matrix
			return alwaysReturnArray ? new Object[0] : null;
		}

		// Single result, unwrap and use as new payload. Fairly common.
		if (resultVector.length == 1 && !alwaysReturnArray) {
			return sink(resultVector[0]);
		}

		// Return a full array of output
		return sink(resultVector);
	}
	
	private Object sink(Object obj) throws IOException {
		if(javaSink != null) {
			obj = javaSink.convertObject((ComplexDataObject)obj);
		}
		
		return obj;
	}
	
	private Object[] sink(Object[] objs) throws IOException {
		if(javaSink != null) {
			for(int i = 0; i < objs.length; i++) {
				objs[i] = javaSink.convertObject((ComplexDataObject)objs[i]);
			}
		}
		
		return objs;
	}

	/**
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	protected Transform buildTransform(Message<?> message) throws Exception {
	    Transform xform = this.transform.get();
        if(xform == null) {
            xform = createTransform();
            this.transform.set(xform);
        }
        return xform;	    
	}

	protected Transform createTransform() throws Exception {
		try {
            return transformClass.newInstance();
		} catch (Exception e) {
			logger.error("Could not instantiate Transformer of class ["
					+ transformClass.getName() + "]", e);
			throw e;
		}
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();

		Assert.state(transformClass != null,
				"The transformClass property must not be null");

		try {
			createTransform();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"The provided transform class threw an exception from its default constructor.",
					e);
		}

	}

	public boolean isAlwaysReturnArray() {
		return alwaysReturnArray;
	}

	public void setAlwaysReturnArray(boolean alwaysReturnArray) {
		this.alwaysReturnArray = alwaysReturnArray;
	}
}
