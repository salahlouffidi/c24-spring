/*
 * Copyright 2012 C24 Technologies
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
package biz.c24.io.spring.batch.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import biz.c24.io.api.transform.Transform;
import biz.c24.io.spring.batch.processor.C24TransformItemProcessor;
import biz.c24.io.spring.batch.processor.C24TransformItemProcessorTests.MyEmail;

/**
 * Validate the C24TranformItemProcessor parser
 * 
 * @author Andrew Elmore
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("transform-item-processor.xml")
public class C24TransformItemProcessorParserTests {
	
	@Autowired
	@Qualifier("transformItemProcessor")
	C24TransformItemProcessor transformItemProcessor;

    @Autowired
    @Qualifier("spelTransformItemProcessor")
    C24TransformItemProcessor spelTransformItemProcessor;
	
	@Autowired
	@Qualifier("validatingTransformItemProcessor")
	C24TransformItemProcessor validatingTransformItemProcessor;
	
	@Autowired
	@Qualifier("defaultTransformItemProcessor")
	C24TransformItemProcessor defaultTransformItemProcessor;

	@Autowired
	@Qualifier("javaSinkItemProcessor")
	C24TransformItemProcessor javaSinkItemProcessor;
	
	@Autowired
	Transform transform;
	
	
	private void validateProcessor(C24TransformItemProcessor processor, boolean validating, Class<?> clazz) {
		assertThat(processor.getTransformer(), is(transform));
		assertThat(processor.isValidating(), is(validating));
		if(clazz == null) {
			assertThat(processor.getTargetClass(), nullValue()) ;
		} else {
			assertEquals(clazz, processor.getTargetClass());
		}
	}
	
	@Test
	public void validateParser() {
		
		validateProcessor(defaultTransformItemProcessor, false, null);
		validateProcessor(transformItemProcessor, false, null);
        validateProcessor(spelTransformItemProcessor, true, null);
        validateProcessor(validatingTransformItemProcessor, true, null);
		validateProcessor(javaSinkItemProcessor, false, MyEmail.class);	
	}
	
	

}
