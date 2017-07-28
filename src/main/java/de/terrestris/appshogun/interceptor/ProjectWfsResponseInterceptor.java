package de.terrestris.appshogun.interceptor;

import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WfsResponseInterceptorInterface;
import de.terrestris.shogun2.util.model.Response;

/**
 * This class demonstrates how to implement the WfsResponseInterceptorInterface.
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
public class ProjectWfsResponseInterceptor implements WfsResponseInterceptorInterface{

	@Override
	public Response interceptGetCapabilities(MutableHttpServletRequest request, Response response) {
		return response;
	}

	@Override
	public Response interceptDescribeFeatureType(MutableHttpServletRequest request, Response response) {
		return response;
	}

	@Override
	public Response interceptGetFeature(MutableHttpServletRequest request, Response response) {
		return response;
	}

	@Override
	public Response interceptLockFeature(MutableHttpServletRequest request, Response response) {
		return response;
	}

	@Override
	public Response interceptTransaction(MutableHttpServletRequest request, Response response) {
		return response;
	}

}
