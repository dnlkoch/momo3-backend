package de.terrestris.appshogun.security.access.entity;

import de.terrestris.shogun2.model.PersistentObject;

/**
 * @author Nils Bühner
 *
 */
public abstract class AbstractProjectPersistentObjectPermissionEvaluatorTest<E extends PersistentObject> {

	// the permission evaluator to test
	protected ProjectPersistentObjectPermissionEvaluator<E> projectPersistentObjectPermissionEvaluator;

	protected final Class<E> entityClass;

	protected E entityToCheck;

	/**
	 * Constructor that has to be implemented by subclasses
	 *
	 * @param entityClass
	 */
	protected AbstractProjectPersistentObjectPermissionEvaluatorTest(
			Class<E> entityClass,
			ProjectPersistentObjectPermissionEvaluator<E> projectPersistentObjectPermissionEvaluator,
			E entityToCheck) {
		this.entityClass = entityClass;
		this.projectPersistentObjectPermissionEvaluator = projectPersistentObjectPermissionEvaluator;
		this.entityToCheck = entityToCheck;
	}

}
