package de.terrestris.appshogun.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.shogun2.converter.PersistentObjectIdResolver;
import de.terrestris.shogun2.dao.LayerDao;
import de.terrestris.shogun2.service.LayerService;

/**
 *
 * @author Nils Buehner
 * @author Daniel Koch
 *
 */
public class ProjectLayerIdResolver<E extends ProjectLayer, D extends LayerDao<E>, S extends LayerService<E, D>> extends
		PersistentObjectIdResolver<E, D, S> {

	@Override
	@Autowired
	@Qualifier("projectLayerService")
	public void setService(S service) {
		this.service = service;
	}

}
