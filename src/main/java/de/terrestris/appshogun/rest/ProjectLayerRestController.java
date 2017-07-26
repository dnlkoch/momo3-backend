package de.terrestris.appshogun.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.appshogun.dao.ProjectLayerDao;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.service.ProjectLayerService;
import de.terrestris.shogun2.rest.AbstractRestController;

/**
 *
 * @author Nils Bühner
 * @author Daniel Koch
 *
 */
@RestController
@RequestMapping("/projectlayers")
public class ProjectLayerRestController<E extends ProjectLayer, D extends ProjectLayerDao<E>, S extends ProjectLayerService<E, D>>
		extends AbstractRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectLayerRestController() {
		this((Class<E>) ProjectLayer.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectLayerRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectLayerService")
	public void setService(S service) {
		this.service = service;
	}

	@Override
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<E> delete(@PathVariable int id) {
        try {
            ProjectLayer entityToDelete = this.service.findById(id);

            this.service.deleteProjectLayer(entityToDelete);

            LOG.trace("Deleted ProjectLayer with ID " + id);
            return new ResponseEntity<E>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            LOG.error("Error deleting ProjectLayer with ID " + id + ": "
                    + e.getMessage());
            return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
        }
	}
}
