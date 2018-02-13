package com.alex.rpc.sunrpc.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerBooter {

	public static void main(String[] args) {
		System.out.println("---Sample Servere Boot----");
		new ClassPathXmlApplicationContext("spring.xml");

	}

}
