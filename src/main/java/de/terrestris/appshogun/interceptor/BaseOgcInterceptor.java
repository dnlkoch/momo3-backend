package de.terrestris.appshogun.interceptor;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import de.terrestris.appshogun.dao.ProjectLayerDao;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.service.ProjectLayerService;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.application.Shogun2ContextUtil;
import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;

public class BaseOgcInterceptor {

	/**
	 * The Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(BaseOgcInterceptor.class);

	/**
	 *
	 */
	private URI appUri = null;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectLayerService")
	protected ProjectLayerService<ProjectLayer, ProjectLayerDao<ProjectLayer>> projectLayerService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userService")
	protected UserService<User, UserDao<User>> userService;

	/**
	 *
	 */
	@Value("${project.publicInterceptGeoServerAction}")
	protected String geoserverInterceptorUrl;

	/**
	 *
	 * @param request
	 */
	protected void setAppUriFromRequest(MutableHttpServletRequest request) {
		if (appUri == null) {
			try {
				appUri = Shogun2ContextUtil.getApplicationURIFromRequest(request);
			} catch (URISyntaxException e) {
				// pass
			}
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	protected MutableHttpServletRequest canGetLayerOrForbidRequest(MutableHttpServletRequest request) {
		try {
			LOGGER.debug("Try to get layer from request");
			this.setAppUriFromRequest(request);
			String url = this.geoserverInterceptorUrl;
			String typeName = MutableHttpServletRequest.getRequestParameterValue(
					request, "TYPENAME");
			projectLayerService.findByUrlAndLayerNames(url, typeName);
		} catch (Exception e) {
			request = forbidRequest(request);
		}
		return request;
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	protected MutableHttpServletRequest forbidRequest(MutableHttpServletRequest request) {
		this.setAppUriFromRequest(request);
		String redirectUri = appUri == null ? "" : appUri.toString();
		request.setRequestURI(redirectUri + "/response/forbidden.action");
		return request;
	}
}
