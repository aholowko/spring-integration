/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.router;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.integration.MessagingConfigurationException;
import org.springframework.integration.channel.ChannelMapping;
import org.springframework.integration.channel.ChannelMappingAware;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.endpoint.SimpleMethodInvoker;
import org.springframework.integration.handler.AbstractMessageHandlerAdapter;
import org.springframework.integration.handler.annotation.Router;
import org.springframework.integration.message.Message;
import org.springframework.util.StringUtils;

/**
 * MessageHandler adapter for methods annotated with {@link Router @Router}.
 * 
 * @author Mark Fisher
 */
public class RouterMessageHandlerAdapter extends AbstractMessageHandlerAdapter implements ChannelMappingAware {

	private Router routerAnnotation;

	private Method method;

	private ChannelMapping channelMapping;


	public RouterMessageHandlerAdapter(Object object, Method method, Router routerAnnotation) {
		this.setObject(object);
		this.method = method;
		this.routerAnnotation = routerAnnotation;
	}

	public void setChannelMapping(ChannelMapping channelMapping) {
		this.channelMapping = channelMapping;
	}

	@Override
	protected Object doHandle(Message message, SimpleMethodInvoker invoker) {
		if (method.getParameterTypes().length != 1) {
			throw new MessagingConfigurationException(
					"method must accept exactly one parameter");
		}
		String propertyName = routerAnnotation.property();
		String attributeName = routerAnnotation.attribute();
		Object retval = null;
		if (StringUtils.hasText(propertyName)) {
			if (StringUtils.hasText(attributeName)) {
				throw new MessagingConfigurationException(
						"cannot accept both 'property' and 'attribute'");
			}
			String property = message.getHeader().getProperty(propertyName);
			retval = this.invokeMethod(invoker, property);
		}
		else if (StringUtils.hasText(attributeName)) {
			Object attribute = message.getHeader().getAttribute(attributeName);
			retval = this.invokeMethod(invoker, attribute);
		}
		else {
			Class<?> type = method.getParameterTypes()[0];
			if (type.equals(Message.class)) {
				retval = this.invokeMethod(invoker, message);
			}
			retval = this.invokeMethod(invoker, message.getPayload());
		}
		if (retval != null) {
			if (retval instanceof Collection) {
				Collection<?> channels = (Collection<?>) retval;
				for (Object channel : channels) {
					if (channel instanceof MessageChannel) {
						this.sendMessage(message, (MessageChannel) channel);
					}
					else if (channel instanceof String) {
						this.sendMessage(message, (String) channel);
					}
					else {
						throw new MessagingConfigurationException(
								"router method must return type 'MessageChannel' or 'String'");
					}
				}
			}
			else if (retval instanceof MessageChannel[]) {
				for (MessageChannel channel : (MessageChannel[]) retval) {
					this.sendMessage(message, channel);
				}
			}
			else if (retval instanceof String[]) {
				for (String channelName : (String[]) retval) {
					this.sendMessage(message, channelName);
				}
			}
			else if (retval instanceof MessageChannel) {
				this.sendMessage(message, (MessageChannel) retval);
			}
			else if (retval instanceof String) {
				this.sendMessage(message, (String) retval);
			}
			else {
				throw new MessagingConfigurationException(
						"router method must return type 'MessageChannel' or 'String'");
			}
		}
		return null;
	}

	private Object invokeMethod(SimpleMethodInvoker<?> invoker, Object parameter) {
		if (this.logger.isDebugEnabled()) {
			logger.debug("invoking method '" + method.getName() + "' with parameter of type '" +
					parameter.getClass().getName() + "'");
		}
		return invoker.invokeMethod(parameter);
	}

	private boolean sendMessage(Message<?> message, String channelName) {
		MessageChannel channel = this.channelMapping.getChannel(channelName);
		if (channel == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("unable to resolve channel for name '" + channelName + "'");
			}
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("sending message to channel '" + channelName + "'");
		}
		return this.sendMessage(message, channel);
	}

	private boolean sendMessage(Message<?> message, MessageChannel channel) {
		if (logger.isDebugEnabled()) {
			logger.debug("sending message to channel '" + channel + "'");
		}
		return channel.send(message);
	}

}
