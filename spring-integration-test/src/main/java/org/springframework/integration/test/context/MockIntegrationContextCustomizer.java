/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.integration.test.context;

import java.beans.Introspector;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

/**
 * The {@link ContextCustomizer} implementation for Spring Integration specific environment.
 * <p>
 * Registers {@link MockIntegrationContext}, {@link IntegrationEndpointsInitializer} beans.
 *
 * @author Artem Bilan
 *
 * @since 5.0
 */
class MockIntegrationContextCustomizer implements ContextCustomizer {

	private final SpringIntegrationTest springIntegrationTest;

	MockIntegrationContextCustomizer(SpringIntegrationTest springIntegrationTest) {
		this.springIntegrationTest = springIntegrationTest;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		registry.registerBeanDefinition(MockIntegrationContext.MOCK_INTEGRATION_CONTEXT_BEAN_NAME,
				new RootBeanDefinition(MockIntegrationContext.class));

		String endpointsInitializer = Introspector.decapitalize(IntegrationEndpointsInitializer.class.getSimpleName());
		registry.registerBeanDefinition(endpointsInitializer,
				BeanDefinitionBuilder.genericBeanDefinition(IntegrationEndpointsInitializer.class)
						.addConstructorArgValue(this.springIntegrationTest)
						.getBeanDefinition());

	}

}

