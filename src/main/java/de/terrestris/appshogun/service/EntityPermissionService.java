package de.terrestris.appshogun.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.dao.ProjectLayerDao;
import de.terrestris.appshogun.dao.ProjectUserDao;
import de.terrestris.appshogun.dao.ProjectUserGroupDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.model.security.EntityPermissionEnvelope;
import de.terrestris.appshogun.model.security.EntityPermissionTypeEnvelope;
import de.terrestris.appshogun.util.config.ProjectConfigHolder;
import de.terrestris.shogun2.dao.GenericHibernateDao;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import javassist.NotFoundException;

@Service
@Qualifier("entityPermissionService")
@Transactional(value = "transactionManager")
public class EntityPermissionService<E extends PersistentObject> {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(EntityPermissionService.class);

	@Autowired
	@Qualifier("projectLayerDao")
	private ProjectLayerDao<? extends ProjectLayer> projectLayerDao;

	@Autowired
	@Qualifier("projectApplicationDao")
	private ProjectApplicationDao<? extends ProjectApplication> projectApplicationDao;

	@Autowired
	@Qualifier("projectUserGroupDao")
	private ProjectUserGroupDao<? extends ProjectUserGroup> projectUserGroupDao;

	@Autowired
	@Qualifier("projectUserDao")
	private ProjectUserDao<? extends ProjectUser> projectUserDao;

	@Autowired
	@Qualifier("permissionAwareCrudService")
	private PermissionAwareCrudService<E, GenericHibernateDao<E,Integer>> permissionAwareCrudService;

	@Autowired
	@Qualifier("projectConfigHolder")
	private ProjectConfigHolder projectConfigHolder;

	/**
	 *
	 * @param entityId
	 * @param entityClass
	 * @param targetEntity
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 */
	@PreAuthorize("hasRole(@projectConfigHolder.getEditorRoleName())")
	public EntityPermissionTypeEnvelope getEntityPermission(Integer entityId,
			String targetEntity, Class<?> entityClass) throws ClassNotFoundException, NotFoundException {

		EntityPermissionTypeEnvelope entityPermissionTypeEnvelope = new EntityPermissionTypeEnvelope();

		if (entityClass.isAssignableFrom(ProjectLayer.class)) {
			// Get the layer entity by the passed ID.
			ProjectLayer layer = projectLayerDao.findById(entityId);

			if (layer == null) {
				throw new NotFoundException("Could not find ProjectLayer with ID " + entityId);
			}

			entityPermissionTypeEnvelope.setTargetEntity(layer);
			entityPermissionTypeEnvelope.setType(entityClass.getSimpleName());

			// Get the permissions, either for group or user.
			if (targetEntity.equalsIgnoreCase("ProjectUserGroup")) {
				Set<EntityPermissionEnvelope> permissions = getUserGroupPermissions(layer);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else if (targetEntity.equalsIgnoreCase("ProjectUser")) {
				Set<EntityPermissionEnvelope> permissions = getUserPermissions(layer);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else {
				throw new NotFoundException(targetEntity + " is not a valid targetEntityClass");
			}
		} else if (entityClass.isAssignableFrom(ProjectApplication.class)) {
			ProjectApplication app = projectApplicationDao.findById(entityId);

			if (app == null) {
				throw new NotFoundException("Could not find ProjectApplication with ID " + entityId);
			}

			entityPermissionTypeEnvelope.setTargetEntity(app);
			entityPermissionTypeEnvelope.setType(entityClass.getSimpleName());

			// Get the permissions, either for group or user.
			if (targetEntity.equalsIgnoreCase("ProjectUserGroup")) {
				Set<EntityPermissionEnvelope> permissions = getUserGroupPermissions(app);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else if (targetEntity.equalsIgnoreCase("ProjectUser")) {
				Set<EntityPermissionEnvelope> permissions = getUserPermissions(app);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else {
				throw new NotFoundException(targetEntity + " is not a valid targetEntityClass");
			}
		} else {
			throw new NotFoundException(entityClass + " is not a valid entityClass");
		}

		return entityPermissionTypeEnvelope;
	}

	/**
	 *
	 * @param envelope
	 * @param classNameOfTarget
	 * @param entityIdOfTarget
	 * @param entityNameOfPermissionHolder
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole(@projectConfigHolder.getEditorRoleName())")
	public EntityPermissionTypeEnvelope createOrUpdateEntityPermission(EntityPermissionTypeEnvelope envelope,
				String classNameOfTarget, Integer entityIdOfTarget, String entityNameOfPermissionHolder)
						throws Exception {

		PersistentObject targetEntity = envelope.getTargetEntity();
		Set<EntityPermissionEnvelope> permissionEnvelopes = envelope.getPermissions();

		// check if path parameters are in sync with object in provided JSON
		checkPathParametersWithEnvelopeObject(envelope, classNameOfTarget, entityIdOfTarget, entityNameOfPermissionHolder);

		if (targetEntity instanceof ProjectLayer) {
			// Get the layer entity by the passed ID.
			ProjectLayer layer = projectLayerDao.findById(targetEntity.getId());
			if (layer == null) {
				throw new NotFoundException("Could not find ProjectLayer with ID " + targetEntity.getId());
			}

			setPermissionsForEntity(layer, permissionEnvelopes);
		} else if (targetEntity instanceof ProjectApplication) {
			ProjectApplication app = projectApplicationDao.findById(targetEntity.getId());
			if (app == null) {
				throw new NotFoundException("Could not find ProjectApplication with ID " + targetEntity.getId());
			}

			setPermissionsForEntity(app, permissionEnvelopes);
		} else {
			throw new NotFoundException(targetEntity.getClass().getSimpleName() + " is not a valid entityClass");
		}

		return getEntityPermission(targetEntity.getId(), entityNameOfPermissionHolder , targetEntity.getClass());
	}

	/**
	 * Helper method to check if path parameters of request are in sync with details provided request object
	 *
	 * @param envelope
	 * @param classNameOfTarget
	 * @param entityIdOfTarget
	 * @param entityNameOfPermissionHolder
	 * @throws Exception
	 */
	private void checkPathParametersWithEnvelopeObject(EntityPermissionTypeEnvelope envelope, String classNameOfTarget,
			Integer entityIdOfTarget, String entityNameOfPermissionHolder) throws Exception {

		LOG.debug("Check if path parameters are in sync with request object...");

		if (entityIdOfTarget.compareTo(envelope.getTargetEntity().getId()) != 0) {
			String msg = "ID of entity " + envelope.getTargetEntity() + " does not match ID "+ entityIdOfTarget + " provided in path.";
			LOG.error(msg);
			throw new Exception(msg);
		}

		final String fullQualifiedClassName = "de.terrestris.appshogun.model." + classNameOfTarget;
		String providedClassNameInJson = envelope.getTargetEntity().getClass().getName();
		if (!StringUtils.equals(providedClassNameInJson, fullQualifiedClassName)) {
			String msg = "Class name in path " + classNameOfTarget + " does not match with class name  "+ providedClassNameInJson + " provided in path.";
			LOG.error(msg);
			throw new Exception(msg);
		}

		final String fullQualifiedClassForEntityPermissionHolder = "de.terrestris.appshogun.model." + entityNameOfPermissionHolder;
		Set<EntityPermissionEnvelope> permissions = envelope.getPermissions();
		for (EntityPermissionEnvelope permission : permissions) {
			String permissionTargetClassName = permission.getTargetEntity().getClass().getName();
			if (permission.getTargetEntity() != null && !StringUtils.equals(permissionTargetClassName, fullQualifiedClassForEntityPermissionHolder)) {
				String msg = "Class name in path " + entityNameOfPermissionHolder + " does not match with class name  "+ permissionTargetClassName + " provided in path.";
				LOG.error(msg);
				throw new Exception(msg);
			}
		}

		LOG.debug("Parameters and request object are in sync.");
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private Set<EntityPermissionEnvelope> getUserGroupPermissions(PersistentObject entity) {

		Map<UserGroup, PermissionCollection> groupPermissions = entity.getGroupPermissions();
		List<? extends ProjectUserGroup> projectUserGroups = projectUserGroupDao.findAll();
		Set<EntityPermissionEnvelope> permissionEnvelopes = new HashSet<EntityPermissionEnvelope>();

		for (ProjectUserGroup projectUserGroup : projectUserGroups) {
			EntityPermissionEnvelope permissionEnvelope = new EntityPermissionEnvelope();
			permissionEnvelope.setTargetEntity(projectUserGroup);
			permissionEnvelope.setType(projectUserGroup.getClass().getSimpleName());
			permissionEnvelope.setDisplayTitle(projectUserGroup.getName());
			permissionEnvelope.setPermissions(groupPermissions.get(projectUserGroup));
			permissionEnvelopes.add(permissionEnvelope);
		}

		return permissionEnvelopes;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private Set<EntityPermissionEnvelope> getUserPermissions(PersistentObject entity) {

		Map<User, PermissionCollection> userPermissions = entity.getUserPermissions();
		List<? extends ProjectUser> projectUsers = projectUserDao.findAll();
		Set<EntityPermissionEnvelope> permissionEnvelopes = new HashSet<EntityPermissionEnvelope>();
		for (ProjectUser projectUser : projectUsers) {
			EntityPermissionEnvelope permissionEnvelope = new EntityPermissionEnvelope();
			permissionEnvelope.setTargetEntity(projectUser);
			permissionEnvelope.setType(projectUser.getClass().getSimpleName());
			permissionEnvelope.setDisplayTitle(projectUser.getFirstName() + " " + projectUser.getLastName());
			permissionEnvelope.setPermissions(userPermissions.get(projectUser));
			permissionEnvelopes.add(permissionEnvelope);
		}

		return permissionEnvelopes;
	}

	/**
	 *
	 * @param entity
	 * @param permissionEnvelopes
	 * @throws NotFoundException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void setPermissionsForEntity(PersistentObject entity, Set<EntityPermissionEnvelope> permissionEnvelopes) throws NotFoundException {

		for (EntityPermissionEnvelope permission : permissionEnvelopes) {
			// Get the permissions, either for group or user.
			PersistentObject targetEntityGroupOrUser = permission.getTargetEntity();
			PermissionCollection permissionsToSet = permission.getPermissions();
			Set<Permission> permissionCollectionSet = null;

			if (permissionsToSet == null) {
				permissionCollectionSet = new HashSet<Permission>();
			} else {
				permissionCollectionSet = permissionsToSet.getPermissions();
			}

			Set<Permission> permissionCollectionSetToRemove = getPermissionsToRemove(permissionCollectionSet);

			Permission[] permissionsArrayToSet = permissionCollectionSet.toArray(new Permission[permissionCollectionSet.size()]);
			Permission[] permissionsArrayToRemove = permissionCollectionSetToRemove.toArray(new Permission[permissionCollectionSetToRemove.size()]);

			if (targetEntityGroupOrUser.getClass().isAssignableFrom(ProjectUserGroup.class)) {
				ProjectUserGroup projectUserGroup = projectUserGroupDao.findById(targetEntityGroupOrUser.getId());
				if (permissionsArrayToSet.length > 0) {
					permissionAwareCrudService.addAndSaveGroupPermissions((E) entity, projectUserGroup, permissionsArrayToSet);
				}
				if (permissionsArrayToRemove.length > 0) {
					permissionAwareCrudService.removeAndSaveGroupPermissions((E) entity, projectUserGroup, permissionsArrayToRemove);
				}
			} else if (targetEntityGroupOrUser.getClass().isAssignableFrom(ProjectUser.class)) {
				ProjectUser projectUser = projectUserDao.findById(targetEntityGroupOrUser.getId());
				if (permissionsArrayToSet.length > 0) {
					permissionAwareCrudService.addAndSaveUserPermissions((E) entity, projectUser, permissionsArrayToSet);
				}
				if (permissionsArrayToRemove.length > 0) {
					permissionAwareCrudService.removeAndSaveUserPermissions((E) entity, projectUser, permissionsArrayToRemove);
				}
			} else {
				throw new NotFoundException("Class of targetEntity currently not mapped (Only ProjectUser and ProjectApplication avaliable).");
			}
		}
	}

	/**
	 *
	 * @param permissionCollectionSet
	 * @return
	 */
	private static Set<Permission> getPermissionsToRemove(Set<Permission> permissionCollectionSet) {

		if (permissionCollectionSet.contains(Permission.ADMIN)) {
			LOG.debug("Requested to set ADMIN permissions.");
			return new HashSet<Permission>();
		}

		Permission[] allPermissions = Permission.values();

		Set<Permission> allPermissionsSet = new HashSet<Permission>(allPermissions.length);

		CollectionUtils.addAll(allPermissionsSet, allPermissions);

		allPermissionsSet.removeAll(permissionCollectionSet);

		return allPermissionsSet;
	}

}
