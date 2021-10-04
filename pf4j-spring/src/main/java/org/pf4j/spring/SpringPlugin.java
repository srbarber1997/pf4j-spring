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

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class SpringPlugin extends Plugin {

    private AnnotationConfigApplicationContext applicationContext;

    public SpringPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.scan(this.getClass().getPackage().getName());
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());

        if (getWrapper().getPluginManager() instanceof SpringPluginManager) {
            SpringPluginManager pluginManager = (SpringPluginManager) getWrapper().getPluginManager();

            applicationContext.setParent(pluginManager.getApplicationContext());
            applicationContext.registerBean(SpringPlugin.class, () -> this);
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            applicationContext.refresh();
        } catch (Exception | LinkageError e) {

        }
        Thread.currentThread().setContextClassLoader(cl);
    }

    @Override
    public void stop() {
        if (applicationContext != null) applicationContext.close();
    }

    public AnnotationConfigApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
