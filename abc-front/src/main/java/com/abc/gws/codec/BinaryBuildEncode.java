package com.abc.gws.codec;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.net.SocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class BinaryBuildEncode extends
		MessageToMessageEncoder<MessageLiteOrBuilder> {

	private static final Logger logger = LoggerFactory
			.getLogger(BinaryBuildEncode.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg,
			List<Object> out) throws Exception {

		ByteBuf result = null;
		if (msg instanceof MessageLite) {
			result = wrappedBuffer(((MessageLite) msg).toByteArray());
		} else if (msg instanceof MessageLite.Builder) {
			result = wrappedBuffer(((MessageLite.Builder) msg).build()
					.toByteArray());
		}

		if (null != result) {
			WebSocketFrame frame = new BinaryWebSocketFrame(result);
			out.add(frame);
			return;
		}

		ChannelFuture future = ctx.close();

		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				SocketAddress addr = ctx.channel().remoteAddress();

				if (future.isSuccess()) {
					logger.info("ctx close: {}", addr);
					return;
				}

				logger.info("ctx close failure: {}", addr);
				ctx.close();
			}
		});
	}

}
