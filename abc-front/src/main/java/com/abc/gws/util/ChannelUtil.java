package com.abc.gws.util;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 *
 */
public final class ChannelUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ChannelUtil.class);

	// 定义一个静态私有变量
	// 不初始化
	// 不使用final关键字
	// 使用volatile保证了多线程访问时instance变量的可见性
	// 避免了instance初始化时其他变量属性还没赋值完时
	// 被另外线程调用
	private static volatile ChannelUtil instance;

	private ChannelUtil() {
		map = new ConcurrentHashMap<String, Channel>();
		all = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	}

	private Map<String, Channel> map;

	private ChannelGroup all;

	/**
	 * 定义一个共有的静态方法
	 *
	 * @return 该类型实例
	 */
	public static ChannelUtil getDefault() {
		// 对象实例化时与否判断
		// 不使用同步代码块
		// instance不等于null时
		// 直接返回对象
		// 提高运行效率
		if (null == instance) {
			// 同步代码块
			// 对象未初始化时
			// 使用同步代码块
			// 保证多线程访问时对象在第一次创建后
			// 不再重复被创建
			synchronized (ChannelUtil.class) {
				// 未初始化
				// 则初始instance变量
				if (null == instance) {
					instance = new ChannelUtil();
				}
			}
		}
		return instance;
	}

	/**
	 *
	 * @param id
	 * @param channel
	 */
	public void putChannel(String id, Channel channel) {
		all.add(channel);
		map.put(id, channel);
	}

	public void removeChannel(String id) {
		all.remove(this.getChannel(id));
		map.remove(id);
	}

	public Channel getChannel(String id) {
		return map.get(id);
	}

	public Map<String, Channel> getChannels() {
		return map;
	}

	public ChannelGroupFuture broadcast(Object message) {
		return all.writeAndFlush(message);
	}

	public ChannelGroupFuture broadcast(Object message, ChannelMatcher matcher) {
		return all.writeAndFlush(message, matcher);
	}

	public ChannelGroup flush() {
		return all.flush();
	}

	public ChannelGroupFuture close() {
		return all.close();
	}

	public int size() {
		return all.size();
	}

}
