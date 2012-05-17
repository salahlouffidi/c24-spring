/*
 * Copyright 2012 C24 Technologies.
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
package biz.c24.io.spring.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Required;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.transform.Transform;

/*
 * A Spring Batch ItemProcesor which invokes a C24 IO Transform to convert a CDO from one model to another
 * 
 * @author Andrew Elmore
 */
public class C24TransformItemProcessor implements ItemProcessor<ComplexDataObject, ComplexDataObject> {

	/*
	 * The C24 IO transform to use
	 */
	private Transform transformer;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public ComplexDataObject process(ComplexDataObject item) throws Exception {
		Object[][] transformedObj = transformer.transform(new Object[][]{{item}});
		return (ComplexDataObject)transformedObj[0][0];
	}

	/*
	 * Get the C24 IO transformer used by this ItemProcessor
	 * 
	 * @return The C24 IO transformer
	 */
	public Transform getTransformer() {
		return transformer;
	}

	/*
	 * Set the C24 IO transformer that this ItemProcessor will use
	 * 
	 * @param transformer The C24 IO transformer to use
	 */
	@Required
	public void setTransformer(Transform transformer) {
		this.transformer = transformer;
	}
	

}