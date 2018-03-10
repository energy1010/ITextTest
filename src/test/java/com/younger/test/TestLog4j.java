package com.younger.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class TestLog4j extends TestCase{

	private static final Logger logger = LoggerFactory.getLogger(TestLog4j.class); 
	
	
	
	public void testLog(){
		logger.trace("trace");
		logger.warn("warn");
		logger.debug("debug");
		logger.info("info");
		logger.error("error");
	}
	
//	public static void main(String[] args) {
//
//	}

}
