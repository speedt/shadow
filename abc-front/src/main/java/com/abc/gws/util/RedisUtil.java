package com.abc.gws.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author Administrator
 *
 */
@PropertySource("classpath:redis.properties")
@Component
public final class RedisUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(RedisUtil.class);

	private static int MAXIDLE;

	private static int MAXWAITMILLIS;

	private static boolean TESTONBORROW;

	private static String HOST;

	private static int PORT;

	private static int TIMEOUT;

	private static String PASS;

	@Value("${db.redis.maxIdle}")
	public void setMaxIdle(int maxIdle) {
		MAXIDLE = maxIdle;
	}

	@Value("${db.redis.maxWaitMillis}")
	public void setMaxWaitMillis(int maxWaitMillis) {
		MAXWAITMILLIS = maxWaitMillis;
	}

	@Value("${db.redis.testOnBorrow:true}")
	public void setTestOnBorrow(boolean testOnBorrow) {
		TESTONBORROW = testOnBorrow;
	}

	@Value("${db.redis.host:127.0.0.1}")
	public void setHost(String host) {
		HOST = host;
	}

	@Value("${db.redis.port:6379}")
	public void setPort(int port) {
		PORT = port;
	}

	@Value("${db.redis.timeout}")
	public void setTimeout(int timeout) {
		TIMEOUT = timeout;
	}

	@Value("${db.redis.pass}")
	public void setPass(String pass) {
		PASS = pass;
	}

	private static JedisPool jedisPool = null;

	// 定义一个静态私有变量(不初始化，不使用final关键字，使用volatile保证了多线程访问时instance变量的可见性，避免了instance初始化时其他变量属性还没赋值完时，被另外线程调用)
	private static volatile RedisUtil instance;

	private RedisUtil() {
	}

	/**
	 * 定义一个共有的静态方法，返回该类型实例
	 *
	 * @return
	 */
	public static RedisUtil getDefault() {
		// 对象实例化时与否判断（不使用同步代码块，instance不等于null时，直接返回对象，提高运行效率）
		if (null == instance) {
			// 同步代码块（对象未初始化时，使用同步代码块，保证多线程访问时对象在第一次创建后，不再重复被创建）
			synchronized (RedisUtil.class) {
				// 未初始化，则初始instance变量
				if (null == instance) {
					instance = new RedisUtil();
				}
			}
		}
		return instance;
	}

	private void initPool() {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(MAXIDLE);
			config.setMaxWaitMillis(MAXWAITMILLIS);
			config.setTestOnBorrow(TESTONBORROW);
			// timeout 最大延迟时间
			jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT, PASS);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 *
	 * @return
	 */
	public Jedis getJedis() {
		if (null == jedisPool) {
			initPool();
		}
		try {
			return null == jedisPool ? null : jedisPool.getResource();
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
}
