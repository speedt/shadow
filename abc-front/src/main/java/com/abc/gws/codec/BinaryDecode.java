package com.abc.gws.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class BinaryDecode extends MessageToMessageDecoder<BinaryWebSocketFrame> {

	@Override
	protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg,
			List<Object> out) throws Exception {
		ByteBuf buf = msg.content();
		out.add(buf);
		buf.retain();
	}

}
