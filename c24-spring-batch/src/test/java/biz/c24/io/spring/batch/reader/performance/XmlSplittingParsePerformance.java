/*
 * Copyright 2012 C24 Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.c24.io.spring.batch.reader.performance;

import java.text.DecimalFormat;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.spring.batch.reader.C24ItemReader;
import biz.c24.io.spring.batch.reader.C24XmlItemReader;
import biz.c24.io.spring.batch.reader.performance.util.ItemReaderJobRunner;
import biz.c24.io.spring.batch.reader.performance.util.MockReader;
import biz.c24.io.spring.batch.reader.performance.util.MockSplittingReaderSource;
import biz.c24.io.spring.batch.reader.source.SplittingReaderSource;
import biz.c24.io.spring.core.C24Model;

/**
 * Performance test C24XmlItemReader 
 * 
 * @author Andrew Elmore
 *
 */
public class XmlSplittingParsePerformance {

    private static C24Model model = new C24Model(biz.c24.retaildemo.model.xml.ReceiptElement.getInstance());
    private static long WARMUP_SECS = 5;
    private static long RUN_SECS = 5;
    
 
    public static void main(String[] args) {        
        
        // Setup
        ItemReaderJobRunner jobRunner = new ItemReaderJobRunner(4) {

            @Override
            protected C24ItemReader<ComplexDataObject> createReader() {
                String data = "<receipt receiptId=\"e16dea4b-5e46-4001-8180-735a862f540e\" customerId=\"37189\" timestamp=\"2012-03-01T00:58:27\">\n" +
                        "\t<item productId=\"258\" quantity=\"8\" price=\"46.96\"/>\n" +
                        "\t<item productId=\"299\" quantity=\"2\" price=\"5.98\"/>\n" +
                        "\t<item productId=\"281\" quantity=\"6\" price=\"23.94\"/>\n" +
                        "\t<item productId=\"279\" quantity=\"6\" price=\"29.94\"/>\n" +
                        "\t<item productId=\"341\" quantity=\"5\" price=\"14.95\"/>\n" +
                        "\t<item productId=\"342\" quantity=\"3\" price=\"8.97\"/>\n" +
                        "\t<item productId=\"320\" quantity=\"4\" price=\"13.96\"/>\n" +
                        "\t<item productId=\"336\" quantity=\"4\" price=\"14.76\"/>\n" +
                        "\t<item productId=\"338\" quantity=\"3\" price=\"8.97\"/>\n" +
                        "\t<item productId=\"343\" quantity=\"2\" price=\"5.98\"/>\n" +
                        "\t<item productId=\"432\" quantity=\"1\" price=\"8.99\"/>\n" +
                        "\t<item productId=\"436\" quantity=\"1\" price=\"8.99\"/>\n" +
                        "\t<item productId=\"442\" quantity=\"2\" price=\"16.0\"/>\n" +
                        "\t<item productId=\"425\" quantity=\"1\" price=\"8.66\"/>\n" +
                        "\t<item productId=\"443\" quantity=\"2\" price=\"17.98\"/>\n" +
                        "\t<item productId=\"430\" quantity=\"1\" price=\"8.99\"/>\n" +
                        "\t<item productId=\"206\" quantity=\"1\" price=\"2\"/>\n" +
                        "</receipt>\n";
                
                MockReader mockReader = new MockReader(data, 100000);
                SplittingReaderSource source = new MockSplittingReaderSource(mockReader, true, true);
                
                C24ItemReader<ComplexDataObject> reader = new C24XmlItemReader<ComplexDataObject>();
               
                reader.setModel(model);
                reader.setElementStartPattern("<receipt .*");
                reader.setElementStopPattern("</receipt>.*");
                reader.setSource(source);
                reader.setValidate(false);
                
                reader.setup(null);
                
                return reader;

            }
            
        };
        
        // Run WarmUp
        long stopTime = System.currentTimeMillis() + (WARMUP_SECS * 1000);
        while(System.currentTimeMillis() < stopTime) {
            jobRunner.runJob();
        }
        
        // Now run the actual test
        
        long cumulativeProcessingTime = 0;
        long numMessages = 0;
        stopTime = System.currentTimeMillis() + (RUN_SECS * 1000);
        
        while(System.currentTimeMillis() < stopTime) {
            jobRunner.runJob();
            cumulativeProcessingTime += jobRunner.getRunTime();
            numMessages += jobRunner.getRecordsProcessed();
        }
        
        DecimalFormat df = new DecimalFormat("#0.00");
        System.out.println("Throughput (msg/sec): " + df.format(numMessages / (cumulativeProcessingTime/(double)1000000000)));
        System.out.println("Mean Latency (us): " + df.format((cumulativeProcessingTime/1000) / (double)numMessages));
        
    }
    
    
}
