package org.pf4j.spring.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PathRequestFacade extends HttpServletRequestWrapper {

    private final String path;

    public PathRequestFacade(HttpServletRequest realRequest, String path) {
        super(realRequest);
        this.path = path;
    }

    @Override
    public String getServletPath() {
        if (StringUtils.isNotBlank(path)) {
            return path;
        }
        return super.getServletPath();
    }

    @Override
    public String getRequestURI() {
        if (StringUtils.isNotBlank(path)) {
            return path;
        }
        return super.getServletPath();
    }
}
