package de.unibayreuth.bayceer.bayeos.gateway;



import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class TimeZoneFilter implements Filter {
		
				
	@Override
	public void init(FilterConfig conf) throws ServletException {		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;				
		if (request.getServletPath().equals("/login") && request.getMethod().equals("POST") && request.getParameter("tz") != null){					
	        request.getSession().setAttribute("tz",req.getParameter("tz"));									        
		}		
		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {				
	}
	
	

}
