package de.terrestris.appshogun.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.appshogun.dao.ProjectUserDao;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.service.ProjectUserService;
import de.terrestris.shogun2.rest.AbstractRestController;


/**
 *
 * terrestris GmbH & Co. KG
 * @author ahenn
 * @date 06.04.2017
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping("/projectusers")
public class ProjectUserRestController<E extends ProjectUser, D extends ProjectUserDao<E>, S extends ProjectUserService<E, D>>
		extends AbstractRestController<E, D, S>  {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserRestController() {
		this((Class<E>) ProjectUser.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectUserRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/* (non-Javadoc)
	 * @see de.terrestris.shogun2.rest.UserRestController#setService(de.terrestris.shogun2.service.UserService)
	 */
	@Override
	@Autowired
	@Qualifier("projectUserService")
	public void setService(S service) {
		this.service = service;
	}

}
