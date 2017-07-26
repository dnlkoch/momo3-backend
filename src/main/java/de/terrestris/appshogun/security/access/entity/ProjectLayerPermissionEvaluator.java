/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.appshogun.dao.LayerTreeDao;
import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.model.tree.LayerTreeFolder;
import de.terrestris.appshogun.service.LayerTreeService;
import de.terrestris.appshogun.service.ProjectApplicationService;
import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class ProjectLayerPermissionEvaluator<E extends ProjectLayer> extends ProjectPersistentObjectPermissionEvaluator<E> {

	@Autowired
	@Qualifier("projectApplicationService")
	private ProjectApplicationService<ProjectApplication, ProjectApplicationDao <ProjectApplication>> projectApplicationService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<TreeNode, LayerTreeDao <TreeNode>> layerTreeService;

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectLayerPermissionEvaluator() {
		this((Class<E>) ProjectLayer.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ProjectLayerPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E layer, Permission permission) {

		// all users but default users are allowed to create layers
		if (permission.equals(Permission.CREATE) &&
				(layer == null || layer.getId() == null) &&
				! ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser()) {
			return true;
		}

		// permit always read on the osm-wms layer, as its needed in application
		// creation process...
		if (permission.equals(Permission.READ) && layer.getName() != null &&
				layer.getName().equalsIgnoreCase("OSM-WMS GRAY")) {
			return true;
		}

		boolean hasGrantedPermissions = hasDefaultProjectPermission(user, layer, permission);

		if (hasGrantedPermissions) {
			return true;
		}

		// check if layer is contained in any application the user is allowed to see
		List<ProjectApplication> projectApplications = projectApplicationService.findAll();
		for (ProjectApplication projectApp : projectApplications) {
			Integer layerTreeId = projectApp.getLayerTree().getId();
			LayerTreeFolder layerTreeRootNode = (LayerTreeFolder) layerTreeService.findById(layerTreeId);
			List<Layer> mapLayers = null;
			try {
				mapLayers = layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);
			} catch (Exception e) {
				LOG.error("Could not fetch maplayers from referenced application. hasPermission will likely return false");
			}

			if (mapLayers != null && mapLayers.contains(layer)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return the projectApplicationService
	 */
	public ProjectApplicationService<ProjectApplication, ProjectApplicationDao<ProjectApplication>> getProjectApplicationService() {
		return projectApplicationService;
	}

	/**
	 * @param projectApplicationService the projectApplicationService to set
	 */
	public void setProjectApplicationService(
			ProjectApplicationService<ProjectApplication, ProjectApplicationDao<ProjectApplication>> projectApplicationService) {
		this.projectApplicationService = projectApplicationService;
	}

	/**
	 * @return the layerTreeService
	 */
	public LayerTreeService<TreeNode, LayerTreeDao<TreeNode>> getLayerTreeService() {
		return layerTreeService;
	}

	/**
	 * @param layerTreeService the layerTreeService to set
	 */
	public void setLayerTreeService(LayerTreeService<TreeNode, LayerTreeDao<TreeNode>> layerTreeService) {
		this.layerTreeService = layerTreeService;
	}

}
