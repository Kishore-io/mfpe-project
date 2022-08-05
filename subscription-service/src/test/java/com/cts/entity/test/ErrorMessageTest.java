package com.cts.entity.test;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cts.entity.ErrorMessage;



 class ErrorMessageTest
{
	LocalDateTime date = LocalDateTime.now();
	ErrorMessage response1 = new ErrorMessage();
	ErrorMessage response2 = new ErrorMessage(HttpStatus.OK,date,"Success");
	
	@Test
	void testMessage() {
		response1.setMessge("Success");
		assertEquals("Success", response1.getMessge());
	}
	
	@Test
	void testDate() {
		response1.setTimestamp(date);
		assertEquals(date, response1.getTimestamp());
	}
	
	@Test
	void testHttpstatus() {
		response1.setStatus(HttpStatus.OK);
		assertEquals(HttpStatus.OK, response1.getStatus());
	}
}