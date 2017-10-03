package com.abc.gws.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.abc.gws.model.LoginModel;
import com.abc.gws.model.ProtocolModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class EchoHandler extends SimpleChannelInboundHandler<ProtocolModel> {

	private static final Logger logger = LoggerFactory
			.getLogger(EchoHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelActive");
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelInactive");
		super.channelInactive(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelReadComplete");
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelRegistered");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelUnregistered");
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		logger.info("channelWritabilityChanged");
		super.channelWritabilityChanged(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtocolModel msg)
			throws Exception {
		logger.info(msg.getSeqId() + ":" + msg.getTimestamp());

		JsonObject jo = new JsonParser().parse(msg.getData()).getAsJsonObject();
		logger.info(jo.get("user_name").getAsString() + ":"
				+ jo.get("user_pass").getAsString());

		ProtocolModel model = new ProtocolModel();
		model.setVersion(msg.getVersion());
		model.setMethod(msg.getMethod());
		model.setSeqId(msg.getSeqId());
		model.setTimestamp(System.currentTimeMillis());

		LoginModel login = new LoginModel();
		login.setUser_name("黄新");
		login.setUser_pass("123456");

		Gson gson = new Gson();
		model.setData(gson.toJson(login));
		ctx.writeAndFlush(model);
	}

}
