package de.terrestris.appshogun.util.serializer;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.appshogun.dao.LayerTreeDao;
import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.dao.ProjectUserDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.model.tree.LayerTreeFolder;
import de.terrestris.appshogun.security.access.entity.ProjectPersistentObjectPermissionEvaluator;
import de.terrestris.appshogun.service.LayerTreeService;
import de.terrestris.appshogun.service.ProjectApplicationService;
import de.terrestris.appshogun.service.ProjectUserService;
import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @author Daniel Koch
 * @date 31.03.2017
 *
 * Custom serializer for instances of {@link ProjectUser}
 */
public class ProjectLayerSerializer extends StdSerializer<ProjectLayer>{

	private static final long serialVersionUID = 1L;

	@Autowired
	@Qualifier("projectUserService")
	private ProjectUserService<ProjectUser, ProjectUserDao <ProjectUser>> projectUserService;

	@Autowired
	@Qualifier("projectApplicationService")
	private ProjectApplicationService<ProjectApplication, ProjectApplicationDao <ProjectApplication>> projectApplicationService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<TreeNode, LayerTreeDao <TreeNode>> layerTreeService;

	/**
	 *
	 */
	public ProjectLayerSerializer(){
		this(null);
	}

	public ProjectLayerSerializer(Class<ProjectLayer> t) {
		super(t);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}


	@Override
	public void serialize(ProjectLayer projectLayer, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();

		generator.writeNumberField("id", projectLayer.getId());
		generator.writeBooleanField("chartable", projectLayer.getChartable() != null ? projectLayer.getChartable() : false);
		generator.writeStringField("dataType", projectLayer.getDataType() != null ? projectLayer.getDataType() : StringUtils.EMPTY);
		generator.writeStringField("description", projectLayer.getDescription() != null ? projectLayer.getDescription() : StringUtils.EMPTY);
		generator.writeBooleanField("hoverable", projectLayer.getHoverable() != null ? projectLayer.getHoverable() : false);
		generator.writeStringField("metadataIdentifier", projectLayer.getMetadataIdentifier() != null ? projectLayer.getMetadataIdentifier() : StringUtils.EMPTY);
		generator.writeStringField("name", projectLayer.getName());
		generator.writeBooleanField("spatiallyRestricted", projectLayer.getSpatiallyRestricted() != null ? projectLayer.getSpatiallyRestricted() : false);
		generator.writeObjectField("appearance", projectLayer.getAppearance());
		generator.writeObjectField("owner", projectLayer.getOwner());
		generator.writeObjectField("source", projectLayer.getSource());

		boolean readPermissionGrantedFromAnyApplication = false;
		ProjectUser currentUser = projectUserService.getUserBySession();
		ProjectPersistentObjectPermissionEvaluator<ProjectLayer> permissionObjectEvaluator = new ProjectPersistentObjectPermissionEvaluator<ProjectLayer>();
		boolean userHasReadPermission = permissionObjectEvaluator.hasPermission(currentUser, projectLayer, Permission.READ);

		if (!userHasReadPermission && !ProjectSecurityUtil.currentUserIsSuperAdmin()) {
			// check if layer is contained in any application the user is allowed to see
			List<ProjectApplication> projectApplications = projectApplicationService.findAll();
			for (ProjectApplication projectApp : projectApplications) {
				Integer layerTreeId = projectApp.getLayerTree().getId();
				LayerTreeFolder layerTreeRootNode = (LayerTreeFolder) layerTreeService.findById(layerTreeId);
				List<Layer> mapLayers = null;
				try {
					mapLayers = this.layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (mapLayers != null && mapLayers.contains(projectLayer)) {
					readPermissionGrantedFromAnyApplication = true;
				}
			}
		}

		generator.writeBooleanField("readPermissionGrantedFromAnyApplication", readPermissionGrantedFromAnyApplication);
		generator.writeEndObject();
	}

}
