package com.abc.gws.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

import com.abc.gws.initializer.WsNormalInitializer;
import com.abc.gws.util.ChannelUtil;
import com.abc.gws.util.Constants;
import com.abc.gws.util.RedisUtil;
import com.abc.util.Server;

/**
 *
 * @author Administrator
 *
 */
@PropertySource("classpath:activemq.properties")
@PropertySource("classpath:redis.properties")
@Component
public class WsNormalServer extends Server {

	@Value("${sha.server.open}")
	private String sha_server_open;

	@Value("${sha.server.close}")
	private String sha_server_close;

	@Value("${db.redis.database}")
	private String db_redis_database;

	private static final Logger logger = LoggerFactory
			.getLogger(WsNormalServer.class);

	@Value("${server.port:1234}")
	private int port;

	@Value("${server.bossThread:2}")
	private int bossThread;

	@Value("${server.workerThread:8}")
	private int workerThread;

	@Value("${server.so.backlog:1024}")
	private int so_backlog;

	@Value("${server.id}")
	private String server_id;

	@Value("${queue.front.start}")
	private String queue_front_start;

	@Value("${queue.front.stop}")
	private String queue_front_stop;

	@Resource(name = "wsNormalInitializer")
	private WsNormalInitializer wsNormalInitializer;

	private ChannelFuture f;
	private EventLoopGroup bossGroup, workerGroup;

	@Resource(name = "jmsMessagingTemplate")
	private JmsMessagingTemplate jmsMessagingTemplate;

	@Override
	public void start() {

		if (!beforeStart())
			return;

		bossGroup = new NioEventLoopGroup(bossThread);
		workerGroup = new NioEventLoopGroup(workerThread);

		ServerBootstrap b = new ServerBootstrap();

		b.localAddress(port);
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);

		b.option(ChannelOption.SO_BACKLOG, so_backlog);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.option(ChannelOption.TCP_NODELAY, true);

		// b.handler(new LoggingHandler(LogLevel.INFO));

		b.childHandler(wsNormalInitializer);

		try {
			f = b.bind().sync();
			if (f.isSuccess()) {
				logger.info("ws start {}", port);
				afterStart();
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					beforeShut();
					shutdown();
				}
			});
		}
	}

	@Override
	public void shutdown() {
		if (null != f) {
			f.channel().close().syncUninterruptibly();
		}
		if (null != bossGroup) {
			bossGroup.shutdownGracefully();
		}
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}

	private void beforeShut() {
		ChannelUtil.getDefault().close();

		List<String> s = new ArrayList<String>();
		s.add(db_redis_database);
		s.add(server_id);

		List<String> b = new ArrayList<String>();

		Jedis j = RedisUtil.getDefault().getJedis();

		if (null == j)
			return;

		Object o = j.evalsha(sha_server_close, s, b);
		j.close();

		String str = o.toString();

		if (Constants.OK.equals(str)) {
			jmsMessagingTemplate.convertAndSend(queue_front_stop, server_id);
			logger.info("front amq stop: {}", server_id);
		}
	}

	private void afterStart() {
		jmsMessagingTemplate.convertAndSend(queue_front_start, server_id);
		logger.info("front amq start: {}", server_id);
	}

	private boolean beforeStart() {

		List<String> s = new ArrayList<String>();
		s.add(db_redis_database);
		s.add(server_id);

		List<String> b = new ArrayList<String>();
		b.add(String.valueOf(System.currentTimeMillis()));

		Jedis j = RedisUtil.getDefault().getJedis();

		if (null == j)
			return false;

		Object o = j.evalsha(sha_server_open, s, b);
		j.close();

		String str = o.toString();

		return Constants.OK.equals(str);
	}

}
