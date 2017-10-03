package com.abc.gws;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.abc.gws.server.WsNormalServer;

/**
 *
 * @author Administrator
 *
 */
@SpringBootApplication
@ComponentScan("com.abc")
public class RunWsNormalServer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory
			.getLogger(RunWsNormalServer.class);

	@Resource(name = "wsNormalServer")
	private WsNormalServer wsNormalServer;

	public static void main(String[] args) {
		SpringApplication.run(RunWsNormalServer.class, args);
	}

	public void run(String... strings) throws Exception {
		try {
			wsNormalServer.start();
			Thread.currentThread().join();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
