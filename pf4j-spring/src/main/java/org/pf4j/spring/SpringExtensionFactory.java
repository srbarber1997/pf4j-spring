package org.pf4j.spring;

import org.pf4j.DefaultExtensionFactory;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

public class SpringExtensionFactory implements ExtensionFactory {

    private final DefaultExtensionFactory defaultExtensionFactory = new DefaultExtensionFactory();

    private final SpringPluginManager pluginManager;

    public SpringExtensionFactory(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
        return pluginWrapper == null
            ? systemExtension(extensionClass)
            : pluginExtension(extensionClass, pluginWrapper);
    }

    private <T> T systemExtension(Class<T> extensionClass) {
        return makeBean(extensionClass, pluginManager.getApplicationContext());
    }

    private <T> T pluginExtension(Class<T> extensionClass, PluginWrapper wrapper) {
        if (wrapper.getPlugin() instanceof SpringPlugin) {
            SpringPlugin springPlugin = (SpringPlugin) wrapper.getPlugin();

            return makeBean(extensionClass, springPlugin.getApplicationContext());
        }
        return defaultExtensionFactory.create(extensionClass);
    }

    private <T> T makeBean(Class<T> extensionClass, ApplicationContext applicationContext) {
        if (applicationContext instanceof AbstractApplicationContext
            && !((AbstractApplicationContext) applicationContext).isActive()) {
            return null;
        }

        try {
            return applicationContext.getAutowireCapableBeanFactory()
                .getBean(extensionClass);
        } catch (NoSuchBeanDefinitionException e) {
            if (isSpringBeanExtension(extensionClass)) {
                // If this is a Spring Bean extension, the bean is
                // likely not meant to exist.
                return null;
            }
        }
        return applicationContext.getAutowireCapableBeanFactory()
            .createBean(extensionClass);
    }

    private boolean isSpringBeanExtension(Class<?> extensionClass) {
        return Stream.of(Component.class, Service.class, Controller.class, Repository.class)
            .anyMatch(extensionClass::isAnnotationPresent);
    }
}
