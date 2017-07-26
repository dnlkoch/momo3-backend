package de.terrestris.appshogun.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.dto.ApplicationData;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.service.ProjectApplicationService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.web.ApplicationController;

/**
 * @author Kai Volland
 * @author Nils Bühner
 *
 */
@Controller
@RequestMapping("/projectapps")
public class ProjectApplicationController<E extends ProjectApplication, D extends ProjectApplicationDao<E>, S extends ProjectApplicationService<E, D>>
		extends ApplicationController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectApplicationController() {
		this((Class<E>) ProjectApplication.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectApplicationController(Class<E> entityClass) {
		super(entityClass);
	}

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

	/**
	 *
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="create.action", method = RequestMethod.POST)
	public ResponseEntity<?> createProjectApplication(@RequestBody ApplicationData applicationData) {

		E app = null;
		try {
			app = (E) service.createProjectApplication(applicationData);
		} catch (Exception e) {
			final String msg = e.getMessage();
			LOG.error("Could not create Project application: " + msg);
			return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}

	/**
	 *
	 * @param appId
	 * @param appName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="copy.action", method = RequestMethod.POST)
	public ResponseEntity<?> copyProjectApplication(
            @RequestParam("appId") String appId, @RequestParam("appName") String appName) {
	        E app = null;
	        Integer appIdInt = Integer.valueOf(appId);
	        try {
	                app = (E) service.copyApp(appIdInt, appName);
	        } catch (Exception e) {
	                final String msg = e.getMessage();
	                LOG.error("Could not copy a layer: " + msg);
	                return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	        return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}

	/**
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="update.action", method = RequestMethod.POST)
	public ResponseEntity<?> updateProjectApplication(@RequestBody ApplicationData applicationData) {

		E app = null;
		Integer appId = applicationData.getId();
		try {
			app = (E) service.updateProjectApplication(appId, applicationData);
		} catch (Exception e) {
			final String msg = e.getMessage();
			LOG.error("Could not update Project application: " + msg);
			return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}
}
