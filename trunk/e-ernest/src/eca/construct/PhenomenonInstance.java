package eca.construct;

import eca.spas.egomem.Place;

/**
 * An instance of phenomenon known to be present in the surrounding environment
 * @author Olivier
 */
public interface PhenomenonInstance {
	
	/**
	 * @return The primitive interaction
	 */
	public PhenomenonType getPhenomenonType();
	
	/**
	 * @param phenomenonType The type of this phenomenon 
	 */
	public void setPhenomenonType(PhenomenonType phenomenonType);
	
	/**
	 * @return The place
	 */
	public Place getPlace();
	
}