/**
 *
 */
package de.terrestris.appshogun.security.access.factory;

import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.model.security.UserGroupRole;
import de.terrestris.appshogun.security.access.entity.ExtentPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.LayerAppearancePermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.MapConfigPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.MapPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ModulePermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectAlwaysAllowReadPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectApplicationPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectLayerPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectPersistentObjectPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectUserGroupPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.ProjectUserPermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.TreeNodePermissionEvaluator;
import de.terrestris.appshogun.security.access.entity.UserGroupRolePermissionEvaluator;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.interceptor.InterceptorRule;
import de.terrestris.shogun2.model.layer.appearance.LayerAppearance;
import de.terrestris.shogun2.model.layer.source.LayerDataSource;
import de.terrestris.shogun2.model.layer.util.Extent;
import de.terrestris.shogun2.model.layer.util.TileGrid;
import de.terrestris.shogun2.model.layout.Layout;
import de.terrestris.shogun2.model.map.MapConfig;
import de.terrestris.shogun2.model.map.MapControl;
import de.terrestris.shogun2.model.module.Map;
import de.terrestris.shogun2.model.module.Module;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.model.token.Token;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.security.access.entity.PermissionCollectionPermissionEvaluator;
import de.terrestris.shogun2.security.access.entity.PersistentObjectPermissionEvaluator;
import de.terrestris.shogun2.security.access.factory.EntityPermissionEvaluatorFactory;

/**
 *
 * This class has to be configured to be used as the permissionEvaluator (of
 * SHOGun2) in the security XML of this project.
 *
 * @author Nils Bühner
 *
 */
public class ProjectPermissionEvaluatorFactory<E extends PersistentObject> extends EntityPermissionEvaluatorFactory<E> {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PersistentObjectPermissionEvaluator<E> getEntityPermissionEvaluator(
			final Class<E> entityClass) {

		if(ProjectLayer.class.isAssignableFrom(entityClass)) {
			return new ProjectLayerPermissionEvaluator();
		}
		if(LayerAppearance.class.isAssignableFrom(entityClass)) {
			return new LayerAppearancePermissionEvaluator();
		}
		if(ProjectApplication.class.isAssignableFrom(entityClass)) {
			return new ProjectApplicationPermissionEvaluator();
		}
		if(Extent.class.isAssignableFrom(entityClass)) {
			return new ExtentPermissionEvaluator();
		}
		if(MapConfig.class.isAssignableFrom(entityClass)) {
			return new MapConfigPermissionEvaluator();
		}
		if(Map.class.isAssignableFrom(entityClass)) {
			return new MapPermissionEvaluator();
		}
		if(Module.class.isAssignableFrom(entityClass)) {
			return new ModulePermissionEvaluator();
		}
		if(TreeNode.class.isAssignableFrom(entityClass)) {
			return new TreeNodePermissionEvaluator();
		}
		if(ProjectUserGroup.class.isAssignableFrom(entityClass)) {
			return new ProjectUserGroupPermissionEvaluator();
		}
		if(ProjectUser.class.isAssignableFrom(entityClass)) {
			return new ProjectUserPermissionEvaluator();
		}
		if(UserGroupRole.class.isAssignableFrom(entityClass)) {
			return new UserGroupRolePermissionEvaluator();
		}
		if(PermissionCollection.class.isAssignableFrom(entityClass)) {
			return new PermissionCollectionPermissionEvaluator();
		}

		// The following types (and subclasses) may be READ by everyone
		// by default. If a type is not listed here, explicit Permissions
		// have to be set for the entities of these types.
		//
		// NOT listed here (and therefore "fully secured") are the following
		// classes AND (!) their subclasses:
		//
		// * Layer
		// * Application
		// * File
		// * Person
		// * UserGroup
		if(InterceptorRule.class.isAssignableFrom(entityClass) ||
			LayerAppearance.class.isAssignableFrom(entityClass) ||
			LayerDataSource.class.isAssignableFrom(entityClass) ||
			Layout.class.isAssignableFrom(entityClass) ||
			MapControl.class.isAssignableFrom(entityClass) ||
			Role.class.isAssignableFrom(entityClass) ||
			TileGrid.class.isAssignableFrom(entityClass) ||
			Token.class.isAssignableFrom(entityClass)) {

			// always grants READ permission (but no other permission)
			// project specific requirements require implementations
			// of custom permission evaluators
			return new ProjectAlwaysAllowReadPermissionEvaluator();
		}

		// Call default project PermissionEvaluator otherwise.
		return new ProjectPersistentObjectPermissionEvaluator<E>(entityClass);

	}

}
