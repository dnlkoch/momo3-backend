package de.terrestris.appshogun.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

import de.terrestris.appshogun.model.tree.LayerTreeFolder;
import de.terrestris.shogun2.model.Application;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.module.Module;

/**
 *
 * @author Nils Bühner
 *
 */
@Entity
public class ProjectApplication extends Application {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ManyToOne
	@JoinColumn(name="LAYERTREE_ROOTNODE_ID")
	private LayerTreeFolder layerTree;

	/**
	 *
	 */
	@ManyToMany
	@JoinTable(
		name = "PROJECTAPPS_ACTIVETOOLS",
		joinColumns = { @JoinColumn(name = "PROJECTAPP_ID") },
		inverseJoinColumns = { @JoinColumn(name = "ACTIVETOOL_ID") }
	)
	@OrderColumn(name = "IDX")
	private List<Module> activeTools = new ArrayList<Module>();

	/**
	 *
	 */
	@ManyToOne
	private User owner;

	/**
	 *
	 */
	public ProjectApplication() {
	}

	/**
	 * @return the layerTree
	 */
	public LayerTreeFolder getLayerTree() {
		return layerTree;
	}

	/**
	 * @param layerTree the layerTree to set
	 */
	public void setLayerTree(LayerTreeFolder layerTree) {
		this.layerTree = layerTree;
	}

	/**
	 * @return the activeTools
	 */
	public List<Module> getActiveTools() {
		return activeTools;
	}

	/**
	 * @param activeTools the activeTools to set
	 */
	public void setActiveTools(List<Module> activeTools) {
		this.activeTools = activeTools;
	}

	/**
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(User owner) {
		this.owner = owner;
	}

}
