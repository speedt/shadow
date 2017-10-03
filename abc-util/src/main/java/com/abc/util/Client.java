package com.abc.util;

/**
 *
 * @author Administrator
 *
 */
public abstract class Client {

	public abstract void start();

	public abstract void shutdown();

	public void restart() {
		shutdown();
		start();
	}

}
