package ernest;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

import tracing.ITracer;

/**
 * Ernest's static system contains all the landmarks ever created.
 * It offers methods to record and refresh new landmarks.
 * @author ogeorgeon
 */
public class StaticSystem 
{

	/** The Tracer. */
	private ITracer m_tracer = null; 

	/** Ernest's internal clock  */
	private int m_clock;

	/** A list of all the stimulations ever identified. */
	public List<IStimulation> m_stimulations = new ArrayList<IStimulation>(20);
	
	/** A list of all the bundles ever identified. */
	public List<IBundle> m_bundles = new ArrayList<IBundle>(20);
	
	/** The current observations */
	private IObservation m_observation = new Observation();
	
	/** The anticipated observation */
	private IObservation m_anticipation = new Observation();
	
	/**
	 * @param tracer The tracer
	 */
	public void setTracer(ITracer tracer)
	{
		m_tracer = tracer;
	}

	/**
	 * Tick Ernest's clock.
	 * Used to simulate a decay in Static memory
	 */
	public void tick()
	{
		m_tracer.addEventElement("clock", m_clock + "");
		
		m_clock++;
	}
	
	/**
	 * @return Ernest's current time
	 */
	public int getClock()
	{
		return m_clock;
	}
	
	/**
	 * Add a stimulation to static memory if it does not already exist
	 * @param red Component of the landmark's color
	 * @param green Component of the landmark's color
	 * @param blue Component of the landmark's color
	 * @param distance The distance of the stimulation (or intensity)
	 * @return the new stimulation if created or the already existing landmark
	 */
	public IStimulation addStimulation(int red, int green, int blue, int distance)
	{
		IStimulation l = new Stimulation(red,green,blue,distance);
		
		int i = m_stimulations.indexOf(l);
		if (i == -1)
			// The landmark does not exist
			m_stimulations.add(l);
		else 
		{
			// The landmark already exists: return a pointer to it.
			l =  m_stimulations.get(i);
			l.setDistance(distance);
		}
		return l;
	}
	
	/**
	 * Add a bundle to static memory if it does not already exist
	 * @param visualStimulation First stimulation
	 * @param tactileStimulation Second stimulation
	 * @return the new bundle if created or the already existing landmark
	 */
	public IBundle addBundle(Color color, IStimulation tactileStimulation)
	{
		IBundle bundle = new Bundle(color,tactileStimulation);
		
		int i = m_bundles.indexOf(bundle);
		if (i == -1)
		{
			m_bundles.add(bundle);
			Object b = m_tracer.addEventElement("bundle");
			m_tracer.addSubelement(b, "color", bundle.getHexColor());
			m_tracer.addSubelement(b, "tactile", bundle.getTactileStimulation().getValue() + "");
		}
		else 
			// The bundle already exists: return a pointer to it.
			bundle =  m_bundles.get(i);
		
		bundle.setLastTimeBundled(m_clock);

		return bundle;
	}
	
	/**
	 * Add a stimulation to episodic memory if it does not already exist
	 * @param type The stimulation's type
	 * @param value The stimulation's value
	 * @return the new landmark if created or the already existing landmark
	 */
	public IStimulation addStimulation(int type, int value)
	{
		IStimulation l = new Stimulation(type, value);
		
		int i = m_stimulations.indexOf(l);
		if (i == -1)
			// The landmark does not exist
			m_stimulations.add(l);
		else 
			// The landmark already exists: return a pointer to it.
			l =  m_stimulations.get(i);
		return l;
	}
	
	/**
	 * Update the current Observation based on the anticiapted observatio and on to the sensory stimulations.
	 * @param visualCortex The set of visual stimulations in the visual cortex.
	 * @param tactileCortex The set of tactile stimulations in the tactile cortex.
	 * @param kinematicStimulation The kinematic stimulation.
	 * @param gustatoryStimulation The gustatory stimulation.
	 * @return A pointer to the current observation that has been updated.
	 */
	public IObservation adjust(IStimulation[] visualCortex, IStimulation[][] tactileCortex, IStimulation kinematicStimulation, IStimulation gustatoryStimulation)
	{
		m_observation = m_anticipation;

		List<ISalience> saliences = new ArrayList<ISalience>(Ernest.RESOLUTION_COLLICULUS);
		Color frontColor = null;

		// Create a List of the various saliences in the visual field

		for (int i = 0 ; i < Ernest.RESOLUTION_RETINA; i++)
		{
			int nbDirection = 1;
			int sumDirection = i * 10;
			int j = i + 1;
			while ( j < Ernest.RESOLUTION_RETINA && visualCortex[i].equals(visualCortex[j]))
			{
				nbDirection++;
				sumDirection += j * 10;
				j++;
			}	
			ISalience salience = new Salience();
			salience.setDirection((int) (sumDirection / nbDirection + .5));
			salience.setDistance(visualCortex[i].getDistance());
			salience.setSpan(nbDirection);
			salience.setColor(visualCortex[i].getColor());
			salience.setAttractiveness(attractiveness(visualCortex[i]) + 5 * nbDirection );
			saliences.add(salience);
			if (salience.getDirection() >= 50 &&  salience.getDirection() <= 60 && sumDirection >= 3 )
				frontColor = visualCortex[i].getColor();
		}

		// The somatotopic map
		
		m_observation.setMap(tactileCortex);
		m_observation.setTactileMap();
		
		// Add the various saliences in the local map to the list
		boolean peripersonal = false;
		if (m_observation.getBundle(1, 0) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(55);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(1, 0).getColor());
			salience.setAttractiveness(m_observation.getBundle(1, 0).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(0, 0) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(85);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(0, 0).getColor());
			salience.setAttractiveness(m_observation.getBundle(0, 0).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(2, 0) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(25);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(2, 0).getColor());
			salience.setAttractiveness(m_observation.getBundle(2, 0).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(0, 1) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(110);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(0, 1).getColor());
			salience.setAttractiveness(m_observation.getBundle(0, 1).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(2, 1) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(0);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(2, 1).getColor());
			salience.setAttractiveness(m_observation.getBundle(2, 1).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(0, 2) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(140);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(0, 2).getColor());
			salience.setAttractiveness(m_observation.getBundle(0, 2).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		else if (m_observation.getBundle(2, 2) != null)
		{
			ISalience salience = new Salience();
			salience.setDirection(-25);
			salience.setSpan(4);
			salience.setColor(m_observation.getBundle(2, 2).getColor());
			salience.setAttractiveness(m_observation.getBundle(2, 2).getAttractiveness(m_clock) + 20);
			saliences.add(salience);
			peripersonal = true;
		}
		//m_observation.setPeripersonal(peripersonal);
		
		ISalience tactileSalience = m_observation.getTactileSalience();
		if (tactileSalience != null)
			saliences.add(tactileSalience);
		
		// Find the most attractive salience in the list (There is at least a wall)
		
		int maxAttractiveness = 0;
		int direction = 0;
		for (ISalience salience : saliences)
			if (salience.getAttractiveness() > maxAttractiveness)
			{
				maxAttractiveness = salience.getAttractiveness();
				direction = salience.getDirection();
				m_observation.setSalience(salience);
			}

//		double x = 0;
//		double y = 0;
//		//for (IIcon icon : icons)
//		IIcon icon = m_observation.getIcon();
//		{
//			x += icon.getAttractiveness() * Math.cos(Math.PI * (icon.getDirection() - Ernest.CENTER_RETINA) / Ernest.CENTER_RETINA / 2);
//			y += icon.getAttractiveness() * Math.sin(Math.PI * (icon.getDirection() - Ernest.CENTER_RETINA) / Ernest.CENTER_RETINA / 2);
//		}
//		double d = Math.atan(y/x) * Ernest.CENTER_RETINA * 2 / Math.PI  + Ernest.CENTER_RETINA;
//		visualDirection = (int)d;

		m_observation.setAttractiveness(maxAttractiveness);
		m_observation.setDirection(direction);
		
		// Taste
		
		m_observation.setGustatory(gustatoryStimulation);
		
		// Kinematic
		
		m_observation.setConfirmation(kinematicStimulation.equals(m_observation.getKinematic()));
		m_observation.setKinematic(kinematicStimulation);

		// If bump, add the bump stimulation to the bundle where Ernest is standing 
		
		//if (m_observation.getKinematic().equals(Ernest.STIMULATION_KINEMATIC_BUMP) && m_observation.getBundle(1, 1) != null) 
		//	m_observation.getBundle(1, 1).setKinematicStimulation(Ernest.STIMULATION_KINEMATIC_BUMP);
		
		// If the current stimulation does not match the anticipated local map then the local map is cleared.
		// TODO The criteria for deciding whether the matching is correct or incorrect need to be learned ! 

		if (m_observation.getBundle(1, 1) != null && m_observation.getBundle(1, 1).getTactileStimulation().equals(Ernest.STIMULATION_TOUCH_WALL))
			m_observation.clearMap();
		
		// Check peripersonal space.
		
		m_observation.setTactileMap();
		//m_observation.peripersonalSpace(m_clock);

		// Bundle the visual icon with the tactile stimulation in front
		
		if (frontColor != null )
		{
			if (!tactileCortex[1][0].equals(Ernest.STIMULATION_TOUCH_EMPTY))		
			{
				IBundle bundle = addBundle(frontColor, tactileCortex[1][0]);
				m_observation.setFrontBundle(bundle);
			}
		}
		else
			if (tactileCortex[1][0].equals(Ernest.STIMULATION_TOUCH_WALL))		
			{
				IBundle bundle = addBundle(Color.BLACK, Ernest.STIMULATION_TOUCH_WALL);
				bundle.setKinematicStimulation(Ernest.STIMULATION_KINEMATIC_BUMP);
				m_observation.setFrontBundle(bundle);
			}
			
		
		return m_observation;
	}
		
	/**
	 * TODO manage different bundles with more than one visual salience 
	 * @param stimulation The stimulation
	 * @return The motivation value generated by this stimulation.
	 * (Either the bundle's motivation or the base motivation if this stimulation evokes no bundle.
	 */
	private int attractiveness(IStimulation stimulation)
	{
		if (stimulation.getColor().equals(Ernest.COLOR_WALL))
			return 0;
		
		for (IBundle bundle : m_bundles)
			if (bundle.getColor().equals(stimulation.getColor()))
				return bundle.getAttractiveness(m_clock);

		return Ernest.ATTRACTIVENESS_OF_UNKNOWN;
	}

	/**
	 * Generate the anticipated observation from the previous observation and the current intention.
	 * @param schema The schema whose effects we want to anticipate.
	 * @return A pointer to the anticipated observation.
	 */
	public IObservation anticipate(IAct act)
	{
		m_anticipation = new Observation();
		m_anticipation.anticipate(m_observation, act);
		return m_anticipation;
	}

	/**
	 * @return The anticipated observation
	 */
	public IObservation getAnticipation()
	{
		return m_observation;//m_anticipation;
	}
	
	public void resetAnticipation()
	{
		m_anticipation = m_observation;
	}
}
	