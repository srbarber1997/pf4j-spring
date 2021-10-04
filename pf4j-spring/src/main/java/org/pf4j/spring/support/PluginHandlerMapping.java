package org.pf4j.spring.support;

import org.apache.commons.lang.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.spring.event.PluginsStartedEvent;
import org.pf4j.spring.util.PathRequestFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Order(-1)
@Component
public class PluginHandlerMapping implements HandlerMapping, ApplicationListener<PluginsStartedEvent> {

    private final Map<String, RequestMappingHandlerMapping> pluginHandlerMappings;

    private final String pluginContextPath;

    @Autowired
    public PluginHandlerMapping(SpringPluginManager pluginManager) {
        this.pluginHandlerMappings = new HashMap<>();
        this.pluginContextPath = pluginManager.getPluginContextPath();
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        if (request.getRequestURI() == null || !request.getRequestURI().startsWith(pluginContextPath)) {
            return null;
        }

        // Chop off the context path and the leading forward slash so the path looks like: "{pluginId}/{endpoint}"
        String[] path = StringUtils.removeStart(
            StringUtils.removeStart(
                request.getRequestURI(),
                pluginContextPath
            ),
            "/"
        ).split("/", 2);

        if (path.length < 2 || path[0] == null) {
            return null;
        }

        String pluginId = path[0];
        String pluginPath = path[1];

        if (pluginHandlerMappings.containsKey(pluginId)) {
            return pluginHandlerMappings.get(pluginId)
                .getHandler(new PathRequestFacade(request, "/" + pluginPath));
        }

        // There is no @RequestMapping to handle this request;
        return null;
    }

    @Override
    public void onApplicationEvent(PluginsStartedEvent event) {
        registerMappings(event.getPluginManager());
    }

    private void registerMappings(PluginManager pluginManager) {
        pluginHandlerMappings.clear();
        pluginManager.getStartedPlugins().stream()
            .map(PluginWrapper::getPlugin)
            .filter(plugin -> plugin instanceof SpringPlugin)
            .map(SpringPlugin.class::cast)
            .forEach(plugin -> {
                RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
                requestMappingHandlerMapping.setApplicationContext(plugin.getApplicationContext());
                requestMappingHandlerMapping.afterPropertiesSet();

                this.pluginHandlerMappings.put(
                    plugin.getWrapper().getPluginId(),
                    requestMappingHandlerMapping
                );
            });
    }
}
