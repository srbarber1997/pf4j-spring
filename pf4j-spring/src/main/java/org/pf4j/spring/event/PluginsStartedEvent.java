package org.pf4j.spring.event;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.ApplicationEvent;

public class PluginsStartedEvent extends ApplicationEvent {

    public PluginsStartedEvent(SpringPluginManager pluginManager) {
        super(pluginManager);
    }

    public SpringPluginManager getPluginManager() {
        return (SpringPluginManager) source;
    }
}
