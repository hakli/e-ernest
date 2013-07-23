package eca.construct;

import eca.spas.egomem.Place;

/**
 * An instance of phenomenon known to be present in the surrounding environment
 * @author Olivier
 */
public class PhenomenonInstanceImpl implements PhenomenonInstance {

	private PhenomenonType phenomenonType = null;
	private Place place = null;
	
	public PhenomenonInstanceImpl(PhenomenonType phenomenonType, Area preArea){
		this.phenomenonType = phenomenonType;
		this.place = preArea;
	}
	
	public PhenomenonType getPhenomenonType() {
		return this.phenomenonType;
	}

	public Place getPlace() {
		return this.place;
	}

	public void setPhenomenonType(PhenomenonType phenomenonType) {
		this.phenomenonType = phenomenonType;
	}

}
