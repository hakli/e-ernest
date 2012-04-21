package spas;

import imos.IAct;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3f;
import ernest.Ernest;
import ernest.ITracer;

/**
 * The spatial system.
 * Maintains the local space map and the persistence memory.
 * @author Olivier
 */
public class Spas implements ISpas 
{
	
	/** The Tracer. */
	private ITracer m_tracer = null; 
	
	public static int PLACE_BACKGROUND = -1;
	public static int PLACE_SEE = 0;
	public static int PLACE_TOUCH = 1;
	public static int PLACE_FOCUS = 10;
	public static int PLACE_BUMP = 11;
	public static int PLACE_EAT  = 12;
	public static int PLACE_CUDDLE = 13;
	public static int PLACE_PRIMITIVE = 14;
	public static int PLACE_COMPOSITE = 15;
	public static int PLACE_INTERMEDIARY = 16;
	public static int PLACE_EVOKE_PHENOMENON = 17;
	public static int PLACE_PHENOMENON = 18;
	
	public static int SHAPE_CIRCLE = 0;
	public static int SHAPE_TRIANGLE = 1;
	public static int SHAPE_PIE = 2;
	public static int SHAPE_SQUARE = 3;

	/** A list of all the bundles ever identified. */
	public List<IBundle> m_bundles = new ArrayList<IBundle>(20);
	
	/** Ernest's persistence momory  */
	//private PersistenceMemory m_persistenceMemory = new PersistenceMemory();
	
	/** Ernest's local space memory  */
	private LocalSpaceMemory m_localSpaceMemory;
	
	/** The list of saliences generated by Ernest's sensory system  */
	List<IPlace> m_placeList = new ArrayList<IPlace>();
	
	IObservation m_observation;
	
	/** The clock of the spatial system. (updated on each update cycle as opposed to IMOS) */
	private int m_clock;

	public void setTracer(ITracer tracer) 
	{
		m_tracer = tracer;
		//m_persistenceMemory.setTracer(tracer);
		m_localSpaceMemory = new LocalSpaceMemory(this, m_tracer);
	}

	/**
	 * The main routine of the Spatial System that is called on each interaction cycle.
	 * Maintain the local space memory.
	 * Construct bundles and affordances.
	 * Maintain the current observation that is used by IMOS. 
	 * @param interactionPlace The place where the ongoing interaction started.
	 * @param observation The current observation.
	 */
	public void step(IObservation observation, ArrayList<IPlace> places) 
	{		

		m_observation = observation;
		
		// translate and rotate the local space memory;
		
		Vector3f memoryTranslation = new Vector3f(observation.getTranslation());
		memoryTranslation.scale(-1);
		//m_localSpaceMemory.update(memoryTranslation, - observation.getRotation());
		m_localSpaceMemory.update(observation.getTranslation(), observation.getRotation());

		// Create and maintain phenomenon places from interaction places. 
		
		m_localSpaceMemory.phenomenon(places, observation, m_clock);
		
		m_localSpaceMemory.trace();

		// Construct synergies associated with bundles in the peripersonal space.		
		//synergy(interactionPlace, observation);
	}
	
	public int getValue(int i, int j)
	{
		Vector3f position = new Vector3f(1 - j, 1 - i, 0);
		if (m_localSpaceMemory != null)
			return m_localSpaceMemory.getValue(position);
		else
			return 0xFFFFFF;
	}

	public int getAttention()
	{
		int attention;
		if (m_observation == null || m_observation.getFocusPlace() == null)
			attention = Ernest.UNANIMATED_COLOR;
		else
			attention = m_observation.getFocusPlace().getBundle().getValue();

		return attention;
	}
	
	/**
	 * Set the list of saliences from the list provided by VacuumSG.
	 * @param salienceList The list of saliences provided by VacuumSG.
	 */
//	public void setPlaceList(List<IPlace> placeList)
//	{
//		m_placeList = placeList;
//	}
		
	public ArrayList<IPlace> getPlaceList()
	{
		//return m_places;
		return m_localSpaceMemory.getPlaceList();
	}

	public void traceLocalSpace() 
	{
		m_localSpaceMemory.trace();
	}

	public IPlace getFocusPlace() 
	{
		return m_localSpaceMemory.getFocusPlace();
	}

	public IPlace addPlace(Vector3f position, int type, int shape) 
	{
		IPlace place = m_localSpaceMemory.addPlace(null, position);
		place.setFirstPosition(position);
		place.setSecondPosition(position);
		place.setType(type);
		place.setShape(shape);
		place.setUpdateCount(m_clock);
		
		return place;
	}

	public IBundle addBundle(int visualValue, int tactileValue) 
	{
		IBundle bundle = new Bundle(visualValue, tactileValue);//, kinematicValue, gustatoryValue);
		
		int i = m_bundles.indexOf(bundle);
		if (i == -1)
		{
			m_bundles.add(bundle);
			if (m_tracer != null) {
				bundle.trace(m_tracer, "bundle");
			}
		}
		else 
			// The bundle already exists: return a pointer to it.
			bundle =  m_bundles.get(i);
		
		// This bundle is considered confirmed or visited
		if (tactileValue != Ernest.STIMULATION_TOUCH_EMPTY)// || kinematicValue != Ernest.STIMULATION_KINEMATIC_FORWARD || gustatoryValue != Ernest.STIMULATION_GUSTATORY_NOTHING)
			bundle.setLastTimeBundled(m_clock);

		return bundle;
	}

	public IBundle addBundle(int value) 
	{
		IBundle bundle = new Bundle(value);
		
		//int i = m_bundles.indexOf(bundle);
		//if (i == -1)
		//{
			m_bundles.add(bundle);
			//if (m_tracer != null) 
			//	bundle.trace(m_tracer, "bundle");
		//}
		//else 
		//	// The bundle already exists: return a pointer to it.
		//	bundle =  m_bundles.get(i);
		
		return bundle;
	}

	public IBundle addBundle(IAct act)
	{
		IBundle bundle = null;
		
		for (IBundle b : m_bundles)
		{
			if (b.hasAct(act))
				bundle = b;
		}
		
		if (bundle == null)
		{
			bundle = new Bundle(act);
			m_bundles.add(bundle);
			if (m_tracer != null)
				bundle.trace(m_tracer, "bundle");
		}
		return bundle;
	}
	
	public void aggregateBundle(IBundle bundle, IAct act) 
	{			
		// See if this act already belongs to another bundle
		IBundle aggregate = null;
		for (IBundle b : m_bundles)
		{
			if (b != bundle && b.hasAct(act))
				aggregate = b;
		}
		
		if (aggregate == null)
		{
			boolean added = bundle.addAct(act);
			if (m_tracer != null && added)
				bundle.trace(m_tracer, "bundle");
		}
		else
		{
			// Merge this other bundle into the bundle that was already found in the list
			boolean added = false;
			for (IAct a : bundle.getActList())
			{
				//boolean add = aggregate.addAct(a);
				added = aggregate.addAct(a) || added;
			}
			if (m_tracer != null && added)
				bundle.trace(m_tracer, "remove_bundle");
			int i = 0; int in = -1;
			for (IBundle bu : m_bundles)
			{ 
				if (bu == bundle)
					in = i;
				i++;	
			}
			if (in >= 0)
				m_bundles.remove(in);
			// The aggregate bundle becomes associated with the phenomenon.
			bundle = aggregate;
			if (m_tracer != null && added)
				bundle.trace(m_tracer, "bundle");
		}		
	}

	public int getClock() 
	{
		return m_clock;
	}

	public IPlace addPlace(IBundle bundle, Vector3f position) 
	{
		return m_localSpaceMemory.addPlace(bundle, position);
	}

	public void tick() 
	{
		m_clock++;
	}

	public ArrayList<IPlace> getPhenomena() 
	{
		return m_localSpaceMemory.getPhenomena();
	}

	public boolean checkAct(IAct act) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public int getValue(Vector3f position) 
	{
		return m_localSpaceMemory.getValue(position);
	}

	public void initSimulation() 
	{
		m_localSpaceMemory.initSimulation();
	}

	public void translateSimulation(Vector3f translation) 
	{
		m_localSpaceMemory.translateSimulation(translation);
	}

	public void rotateSimulation(float angle) 
	{
		m_localSpaceMemory.rotateSimulation(angle);
	}

	public int getValueSimulation(Vector3f position) 
	{
		return m_localSpaceMemory.getValueSimulation(position);
	}	
	
	public IBundle getBundleSimulation(Vector3f position) 
	{
		return m_localSpaceMemory.getBundleSimulation(position);
	}	
	
	/**
	 * Returns the first bundle found that contains this act.
	 * @param act The act to check.
	 * @return The bundle that match this act.
	 */
	public IBundle evokeBundle(IAct act)
	{
		for (IBundle bundle : m_bundles)
		{
			if (bundle.hasAct(act))
				return bundle;
			// presuppose the value of phenomena
			//if (bundle.getValue() == act.getPhenomenon())
			//	return bundle;
				
		}
		return null;
	}

	/**
	 * Returns the first bundle found form a visual stimulation.
	 * TODO manage different bundles that have the same color.
	 * TODO manage different bundles with more than one visual stimulation.
	 * TODO manage bundles that have no tactile stimulation. 
	 * @param stimulation The visual stimulation.
	 * @return The bundle that match this stimulation.
	 */
	public IBundle seeBundle(int visualValue)
	{
		for (IBundle bundle : m_bundles)
			// Return only bundles that have also a tactile stimulation
			if (bundle.getVisualValue() == visualValue && bundle.getTactileValue() != Ernest.STIMULATION_TOUCH_EMPTY)
				return bundle;

		return null;
	}

	/**
	 * Returns the first bundle found form a tactile stimulation.
	 * TODO evoke different kind of bundles 
	 * @param stimulation The visual stimulation.
	 * @return The bundle that match this stimulation.
	 */
//	public IBundle touchBundle(int tactileValue)
//	{
//		for (IBundle bundle : m_bundles)
//			// So far, only consider bump and eat bundles
//			if (bundle.getTactileValue() == tactileValue && bundle.getAffordanceList().size() > 0)
//					//(bundle.getKinematicValue() != Ernest.STIMULATION_KINEMATIC_FORWARD || bundle.getGustatoryValue() != Ernest.STIMULATION_GUSTATORY_NOTHING))
//				return bundle;
//
//		return null;
//	}

}
