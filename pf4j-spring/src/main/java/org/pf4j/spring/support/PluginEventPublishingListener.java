package org.pf4j.spring.support;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.event.PluginsStartedEvent;
import org.pf4j.spring.event.PluginsStoppedEvent;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

@Component
public class PluginEventPublishingListener implements ApplicationListener<ApplicationEvent> {

    protected final Collection<ApplicationEventMulticaster> eventMulticasters = new HashSet<>();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof PluginsStartedEvent) {
            registerMulticasters(((PluginsStartedEvent) event).getPluginManager());
        }
        if (event instanceof PluginsStoppedEvent) {
            eventMulticasters.clear();
        }

        eventMulticasters.forEach(multicaster -> multicaster.multicastEvent(event));
    }

    private void registerMulticasters(PluginManager pluginManager) {
        eventMulticasters.clear();
        pluginManager.getStartedPlugins().stream()
            .map(PluginWrapper::getPlugin)
            .filter(plugin -> plugin instanceof SpringPlugin)
            .map(SpringPlugin.class::cast)
            .filter(plugin -> plugin.getApplicationContext().isActive())
            .map(plugin -> {
                try {
                    return plugin.getApplicationContext()
                        .getBean(ApplicationEventMulticaster.class);
                } catch (NoSuchBeanDefinitionException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(eventMulticasters::add);
    }
}
