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
package biz.c24.io.spring.batch.reader.performance.util;

import org.springframework.batch.item.ItemReader;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.spring.batch.reader.C24ItemReader;

/**
 * Abstract test harness to call read on a C24ItemReader until the underlying Reader is exhausted.
 * 
 * @author Andrew Elmore
 *
 */
public abstract class ItemReaderJobRunner {
    private long runTime;
    private long recordsProcessed;
    private int numThreads;
    
    public ItemReaderJobRunner(int numThreads) {
        this.numThreads = numThreads;
    }
    
    /**
     * Override this to create the C24ItemReader to be exercised during the test
     * @return The C24ItemReader to be exercised during the test.
     */
    protected abstract C24ItemReader<ComplexDataObject> createReader();
    
    /**
     * Executes the test.
     * Fires up the specified number of threads to invoke read on the C24ItemReader until there
     * is no more data to read.
     * Tracks the number of items read and the cumulative processing time.
     */
    public void runJob() {
        
        ItemReaderJobRunner.Worker<ComplexDataObject>[] workers = new ItemReaderJobRunner.Worker[numThreads]; 
                
        C24ItemReader<ComplexDataObject> reader = createReader();
        
        for(int i=0; i < workers.length; i++) {
            workers[i] = new ItemReaderJobRunner.Worker<ComplexDataObject>();
            workers[i].setReader(reader);
            workers[i].start();
        }
    
        recordsProcessed = 0;
        long startTime = System.nanoTime();
    
        for(int i=0; i < workers.length; i++) {
            try {
                workers[i].join(200000);
                recordsProcessed += workers[i].getCount();
                if(workers[i].getException() != null) {
                    throw new RuntimeException(workers[i].getException());
                }
                
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted waiting on thread");
            }
            if(workers[i].isAlive()) {
                throw new RuntimeException("Timed out waiting for thread to complete its work");
            }
        }
        
        runTime = System.nanoTime() - startTime;
        
        reader.cleanup();
    }
    
    public long getRecordsProcessed() {
        return this.recordsProcessed;
    }
    
    public long getRunTime() {
        return this.runTime;
    }

    
    private static class Worker<T extends ComplexDataObject> implements Runnable {
        private ItemReader<T> reader;
        private Exception ex = null;
        private long count = 0;
        private Thread thread = new Thread(this);
        
        public Worker() {}
        
        public void setReader(ItemReader<T> itemReader) {
            this.reader = itemReader;
        }
        
        public void start() {
            thread.start();
        }
        
        public void join(long timeoutMs) throws InterruptedException {
            thread.join(timeoutMs);
        }
        
        public boolean isAlive() {
            return thread.isAlive();
        }
        
        public long getCount() {
            return count;
        }
        
        public void run() {
            count = 0;
            ex = null;
            try {
                while(reader.read() != null) {
                    count++;                  
                }
            } catch (Exception e) {
                ex = e;
                throw new RuntimeException(e);
            }
        }
        
        public Exception getException() {
            return ex;
        }
    }
    
}