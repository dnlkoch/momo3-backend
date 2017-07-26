package de.terrestris.appshogun.interceptor;

import org.apache.log4j.Logger;

import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WmsResponseInterceptorInterface;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * @author terrestris GmbH & Co. KG
 *
 */
public class ProjectWmsResponseInterceptor implements WmsResponseInterceptorInterface {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(ProjectWmsResponseInterceptor.class);

	/**
	 *
	 */
	@Override
	public Response interceptGetMap(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

	@Override
	public Response interceptGetCapabilities(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

	@Override
	public Response interceptGetFeatureInfo(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

	@Override
	public Response interceptDescribeLayer(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

	@Override
	public Response interceptGetLegendGraphic(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

	@Override
	public Response interceptGetStyles(MutableHttpServletRequest mutableRequest, Response response) {
		return response;
	}

}
