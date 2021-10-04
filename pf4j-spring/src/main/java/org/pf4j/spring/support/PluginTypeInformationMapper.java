package org.pf4j.spring.support;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.SimpleTypeInformationMapper;
import org.springframework.data.mapping.Alias;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

@Component
public class PluginTypeInformationMapper extends SimpleTypeInformationMapper {

    private final SpringPluginManager pluginManager;

    @Autowired
    public PluginTypeInformationMapper(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public TypeInformation<?> resolveTypeFrom(Alias alias) {
        String typeName = alias.mapTyped(String.class);
        if (typeName != null) {
            for (PluginWrapper wrapper : pluginManager.getStartedPlugins()) {
                try {
                    return ClassTypeInformation.from(
                        ClassUtils.forName(
                            typeName,
                            wrapper.getPluginClassLoader()
                        )
                    );
                } catch (ClassNotFoundException ignored) {}
            }
        }

        return null;
    }
}
