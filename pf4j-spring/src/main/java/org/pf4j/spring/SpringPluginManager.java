/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.spring;

import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.spring.event.PluginsStartedEvent;
import org.pf4j.spring.event.PluginsStoppedEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpringPluginManager extends DefaultPluginManager
    implements InitializingBean, DisposableBean, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    private String pluginContextPath = "/plugin-api";

    public SpringPluginManager() {
    }

    public SpringPluginManager(Path... pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    public void afterPropertiesSet() {
        loadPlugins();
        startPlugins();
        applicationContext.publishEvent(new PluginsStartedEvent(this));
    }

    @Override
    public void destroy() {
        stopPlugins();
        applicationContext.publishEvent(new PluginsStoppedEvent(this));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SpringExtensionFactory(this);
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type) {
        return super.getExtensions(type).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getPluginContextPath() {
        return pluginContextPath;
    }

    public void setPluginContextPath(String pluginContextPath) {
        this.pluginContextPath = pluginContextPath;
    }
}
