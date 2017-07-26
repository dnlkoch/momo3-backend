/**
 *
 */
package de.terrestris.appshogun.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.terrestris.appshogun.model.tree.LayerTreeLeaf;
import de.terrestris.appshogun.util.serializer.ProjectLayerSerializer;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.Layer;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
@JsonSerialize(using=ProjectLayerSerializer.class)
public class ProjectLayer extends Layer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private Boolean spatiallyRestricted;

	/**
	 *
	 */
	private Boolean hoverable;

	/**
	 *
	 */
	private Boolean chartable;

	/**
	 *
	 */
	private String dataType;

	/**
	 *
	 */
	private String metadataIdentifier;

	/**
	 *
	 */
	@OneToMany(cascade = CascadeType.REMOVE)
	@JoinColumn(name="LAYER_ID")
	private List<LayerTreeLeaf> layerTreeLeaves;

	/**
	 *
	 */
	@ManyToOne
	private User owner;

	/**
	 *
	 */
	public ProjectLayer() {
	}

	/**
	 * @return the spatiallyRestricted
	 */
	public Boolean getSpatiallyRestricted() {
		return spatiallyRestricted;
	}

	/**
	 * @param spatiallyRestricted the spatiallyRestricted to set
	 */
	public void setSpatiallyRestricted(Boolean spatiallyRestricted) {
		this.spatiallyRestricted = spatiallyRestricted;
	}

	/**
	 * @return the hoverable
	 */
	public Boolean getHoverable() {
		return hoverable;
	}

	/**
	 * @param hoverable the hoverable to set
	 */
	public void setHoverable(Boolean hoverable) {
		this.hoverable = hoverable;
	}

	/**
	 * @return the chartable
	 */
	public Boolean getChartable() {
		return chartable;
	}

	/**
	 * @param chartable the chartable to set
	 */
	public void setChartable(Boolean chartable) {
		this.chartable = chartable;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the metadataIdentifier
	 */
	public String getMetadataIdentifier() {
		return metadataIdentifier;
	}

	/**
	 * @param metadataIdentifier the metadataIdentifier to set
	 */
	public void setMetadataIdentifier(String metadataIdentifier) {
		this.metadataIdentifier = metadataIdentifier;
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

	/**
	 * @see java.lang.Object#hashCode()
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(29, 11).
				appendSuper(super.hashCode()).
				append(getSpatiallyRestricted()).
				append(getHoverable()).
				append(getChartable()).
				append(getDataType()).
				toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProjectLayer))
			return false;
		ProjectLayer other = (ProjectLayer) obj;

		return new EqualsBuilder().
				appendSuper(super.equals(other)).
				append(getSpatiallyRestricted(), other.getSpatiallyRestricted()).
				append(getHoverable(), other.getHoverable()).
				append(getChartable(), other.getChartable()).
				append(getDataType(), other.getDataType()).
				isEquals();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

}
