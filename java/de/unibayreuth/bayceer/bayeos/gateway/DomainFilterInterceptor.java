package de.unibayreuth.bayceer.bayeos.gateway;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class DomainFilterInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
    DomainFilter domainFilter;

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			if (handler instanceof HandlerMethod) {				
				modelAndView.addObject("domainFilter",domainFilter);
			}
		}
	}

}
