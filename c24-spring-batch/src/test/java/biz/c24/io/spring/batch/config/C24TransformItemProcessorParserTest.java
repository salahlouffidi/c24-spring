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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("transform-item-processor.xml")
public class C24TransformItemProcessorParserTest {
	
	@Autowired
	@Qualifier("transformItemProcessor")
	C24TransformItemProcessor transformItemProcessor;
	
	@Autowired
	@Qualifier("validatingTransformItemProcessor")
	C24TransformItemProcessor validatingTransformItemProcessor;
	
	@Autowired
	@Qualifier("defaultTransformItemProcessor")
	C24TransformItemProcessor defaultTransformItemProcessor;
	
	@Autowired
	Transform transform;
	
	
	private void validateProcessor(C24TransformItemProcessor processor, boolean validating) {
		assertThat(processor.getTransformer(), is(transform));
		assertThat(processor.isValidating(), is(validating));
	}
	
	@Test
	public void validateParser() {
		
		validateProcessor(defaultTransformItemProcessor, false);
		validateProcessor(transformItemProcessor, false);
		validateProcessor(validatingTransformItemProcessor, true);		
	}
	
	

}
