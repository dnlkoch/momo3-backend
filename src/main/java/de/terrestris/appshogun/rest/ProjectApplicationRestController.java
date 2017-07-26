package de.terrestris.appshogun.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.service.ProjectApplicationService;
import de.terrestris.shogun2.rest.ApplicationRestController;

/**
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/projectapps")
public class ProjectApplicationRestController<E extends ProjectApplication, D extends ProjectApplicationDao<E>, S extends ProjectApplicationService<E, D>>
		extends ApplicationRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectApplicationService")
	public void setService(S service) {
		this.service = service;
	}

}
