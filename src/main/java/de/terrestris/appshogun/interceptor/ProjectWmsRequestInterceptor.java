package de.terrestris.appshogun.interceptor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import de.terrestris.appshogun.dao.ProjectLayerDao;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.service.ProjectLayerService;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WmsRequestInterceptorInterface;

/**
 *
 * @author terrestris GmbH & Co. KG
 *
 */
public class ProjectWmsRequestInterceptor extends BaseOgcInterceptor implements WmsRequestInterceptorInterface {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(ProjectWmsRequestInterceptor.class);

	/**
	 *
	 */
	@Value("${role.superAdminRoleName}")
	private String adminRoleName;

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetMap(MutableHttpServletRequest request) {
		LOG.debug("Intercepting Project WMS GetMap request");

		String layersParam = request.getParameter("LAYERS");

		// 1. The corresponding SHOGun2 layer (based on URL and layerNames from request)
		ProjectLayer layer = projectLayerService.findByUrlAndLayerNames(geoserverInterceptorUrl, layersParam);

		if(layer == null) {
			LOG.warn("Layer from SHOGun2 database is null!?");
			// TODO maybe returning null is not optimal here
			return null;
		}

		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetCapabilities(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetFeatureInfo(
			MutableHttpServletRequest request) {
		return request;
	}

	@Override
	public MutableHttpServletRequest interceptDescribeLayer(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetLegendGraphic(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetStyles(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 * @return the userService
	 */
	public UserService<User, UserDao<User>> getUserService() {
		return userService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService<User, UserDao<User>> userService) {
		this.userService = userService;
	}

	/**
	 * @return the projectLayerService
	 */
	public ProjectLayerService<ProjectLayer, ProjectLayerDao<ProjectLayer>> getProjectLayerService() {
		return projectLayerService;
	}

	/**
	 * @param projectLayerService the projectLayerService to set
	 */
	public void setProjectLayerService(ProjectLayerService<ProjectLayer, ProjectLayerDao<ProjectLayer>> projectLayerService) {
		this.projectLayerService = projectLayerService;
	}

	/**
	 * @return the geoserverInterceptorUrl
	 */
	public String getGeoserverInterceptorUrl() {
		return geoserverInterceptorUrl;
	}

	/**
	 * @param geoserverInterceptorUrl the geoserverInterceptorUrl to set
	 */
	public void setGeoserverInterceptorUrl(String geoserverInterceptorUrl) {
		this.geoserverInterceptorUrl = geoserverInterceptorUrl;
	}

	/**
	 * @return the adminRoleName
	 */
	public String getAdminRoleName() {
		return adminRoleName;
	}

	/**
	 * @param adminRoleName the adminRoleName to set
	 */
	public void setAdminRoleName(String adminRoleName) {
		this.adminRoleName = adminRoleName;
	}

}
