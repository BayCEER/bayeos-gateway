package de.unibayreuth.bayceer.bayeos.gateway;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class ControllerNameInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (modelAndView != null) {
			if (handler instanceof HandlerMethod) {
				HandlerMethod m = (HandlerMethod) handler;
				modelAndView.addObject("controllerName", m.getBeanType().getSimpleName().replace("Controller", ""));
			} else {
				modelAndView.addObject("controllerName", "");
			}
		}
	}

}
