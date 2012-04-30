package biz.c24.io.spring.integration.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

/**
 * User: porter
 * Date: 28/04/2012
 * Time: 18:51
 */
public class BaseIntegrationTest {

    	protected byte[] loadCsvBytes() throws Exception {

		ClassPathResource resource = new ClassPathResource("valid-1.txt");
		byte[] valid1 = FileCopyUtils
				.copyToByteArray(resource.getInputStream());

		return valid1;
	}


}
