package de.terrestris.appshogun.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.service.ProjectApplicationService;
import de.terrestris.shogun2.converter.PersistentObjectIdResolver;

public class ProjectApplicationIdResolver<E extends ProjectApplication, D extends ProjectApplicationDao<E>, S extends ProjectApplicationService<E, D>>
		extends PersistentObjectIdResolver<E, D, S> {

	@Override
	@Autowired
	@Qualifier("projectApplicationService")
	public void setService(S service) {
		this.service = service;
	}

}
