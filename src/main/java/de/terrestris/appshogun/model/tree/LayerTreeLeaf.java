/**
 *
 */
package de.terrestris.appshogun.model.tree;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.appshogun.converter.ProjectLayerIdResolver;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
public class LayerTreeLeaf extends TreeNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ManyToOne
	@JoinColumn(name="LAYER_ID")
	@JsonIdentityInfo(
		resolver = ProjectLayerIdResolver.class,
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id"
	)
	@JsonIdentityReference(alwaysAsId = true)
	private ProjectLayer layer;

	/**
	 *
	 */
	public LayerTreeLeaf() {

	}

	/**
	 * @return the layer
	 */
	public ProjectLayer getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(ProjectLayer layer) {
		this.layer = layer;
	}

}
