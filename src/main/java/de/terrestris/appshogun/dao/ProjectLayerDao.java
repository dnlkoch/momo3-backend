package de.terrestris.appshogun.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.shogun2.dao.LayerDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("projectLayerDao")
public class ProjectLayerDao<E extends ProjectLayer> extends LayerDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ProjectLayerDao() {
		super((Class<E>) ProjectLayer.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ProjectLayerDao(Class<E> clazz) {
		super(clazz);
	}


	/**
	 * Finds and returns a project layer by the url and the layernames of its
	 * source.
	 *
	 * @param url
	 * @param layerNames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E findByUrlAndLayerNames(String url, String layerNames) {
		if (url == null || layerNames == null) {
			return null;
		}
		Criteria criteria = createDistinctRootEntityCriteria();
		criteria.createAlias("source", "s");
		criteria.add(Restrictions.eq("s.url", url));
		criteria.add(Restrictions.eq("s.layerNames", layerNames));
		return (E) criteria.uniqueResult();
	}

}
