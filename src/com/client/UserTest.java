package com.client;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class UserTest {

	User book=null;
	
	@Before
	public void setup() throws Exception {
		System. out.println("测试开始！");
		 book = new User();
		System.out.println("book对象被初始化！");
	}
	
	@After
	public void tearDown() throws Exception {
	     System.out.println("book对象将被清理！");
	      book = null;
	     System.out.println("测试结束！");
	  }

	
	@Test
	public void testSetUsername() {
		 book.setUsername("ASP"); //设置name属性的值为ASP
		   //使用Assert查看name属性的值是否为ASP，这是个必然出现错误的测试
		 assertEquals("ASP", book.getUsername());
		 System.out.println("Username属性被测试！");
	
	}

	@Test
	public void testSetIP() {
	      book.setIP("001"); //设置id属性的值为001
			 //使用Assert查看id属性的值是否为001
		  assertEquals("001", book.getIP());
		  System.out.println("ip属性被测试！");
	}

	

}
