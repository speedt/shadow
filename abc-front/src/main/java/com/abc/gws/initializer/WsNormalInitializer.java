package com.abc.gws.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.gws.codec.JSONCodecV3;
import com.abc.gws.handler.BlacklistHandler;
import com.abc.gws.handler.ExceptionHandler;
import com.abc.gws.handler.HeartbeatV2Handler;
import com.abc.gws.handler.LoginV2Handler;
import com.abc.gws.handler.ProtocolSafeV2Handler;
import com.abc.gws.handler.TimeV2Handler;
import com.abc.gws.handler.TimeVersionV2Handler;
import com.abc.gws.handler.TimeoutHandler;

/**
 *
 * @author Administrator
 *
 */
@Component
public class WsNormalInitializer extends ChannelInitializer<NioSocketChannel> {

	// @Resource(name = "echoHandler")
	// private EchoHandler echoHandler;

	@Resource
	private JSONCodecV3 jSONCodecV3;

	@Value("${server.idle.readerIdleTime:3}")
	private int readerIdleTime;

	@Value("${server.idle.writerIdleTime:7}")
	private int writerIdleTime;

	@Value("${server.idle.allIdleTime:10}")
	private int allIdleTime;

	@Resource(name = "timeoutHandler")
	private TimeoutHandler timeoutHandler;

	@Resource(name = "blacklistHandler")
	private BlacklistHandler blacklistHandler;

	@Resource(name = "exceptionHandler")
	private ExceptionHandler exceptionHandler;

	@Resource(name = "protocolSafeV2Handler")
	private ProtocolSafeV2Handler protocolSafeV2Handler;

	@Resource(name = "timeVersionV2Handler")
	private TimeVersionV2Handler timeVersionV2Handler;

	@Resource(name = "heartbeatV2Handler")
	private HeartbeatV2Handler heartbeatV2Handler;

	@Resource(name = "loginV2Handler")
	private LoginV2Handler loginV2Handler;

	@Resource(name = "timeV2Handler")
	private TimeV2Handler timeV2Handler;

	@Override
	protected void initChannel(NioSocketChannel ch) throws Exception {
		ChannelPipeline pipe = ch.pipeline();

		// pipe.addLast(new LoggingHandler(LogLevel.INFO));

		pipe.addLast(exceptionHandler);
		pipe.addLast(blacklistHandler);

		pipe.addLast(new IdleStateHandler(readerIdleTime, writerIdleTime,
				allIdleTime, TimeUnit.SECONDS));
		pipe.addLast(timeoutHandler);

		pipe.addLast(new HttpServerCodec());
		pipe.addLast(new HttpObjectAggregator(1024 * 64));
		pipe.addLast(new ChunkedWriteHandler());
		pipe.addLast(new HttpContentCompressor());
		pipe.addLast(protocolSafeV2Handler);
		pipe.addLast(new WebSocketServerProtocolHandler("/", null, false));

		pipe.addLast(new WebSocketServerCompressionHandler());

		pipe.addLast(jSONCodecV3);

		pipe.addLast(timeVersionV2Handler);
		pipe.addLast(loginV2Handler);
		pipe.addLast(heartbeatV2Handler);

		// pipe.addLast(echoHandler);
		pipe.addLast(timeV2Handler);
	}

}
