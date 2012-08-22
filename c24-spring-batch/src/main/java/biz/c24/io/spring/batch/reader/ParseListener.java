package biz.c24.io.spring.batch.reader;

import biz.c24.io.api.data.ComplexDataObject;

public interface ParseListener<Context, Result> {
	
	public String processLine(String line);
	public Context getContext(String element); 
	public Result process(ComplexDataObject obj, Context context);

}
