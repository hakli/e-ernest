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

	/** Ernest's persistence momory  */
	private PersistenceMemory m_persistenceMemory = new PersistenceMemory();
	
	/** Ernest's local space memory  */
	private LocalSpaceMemory m_localSpaceMemory = new LocalSpaceMemory();
	
	/** The visual stimulation in front of Ernest  */
	//ISalience m_frontVisualSalience;

	/** The list of saliences generated by Ernest's sensory system  */
	List<ISalience> m_salienceList = new ArrayList<ISalience>();

	int m_kinematicStimulation = Ernest.STIMULATION_KINEMATIC_FORWARD;
	int m_gustatoryStimulation = Ernest.STIMULATION_GUSTATORY_NOTHING;
	
	public void setTracer(ITracer tracer) 
	{
		m_tracer = tracer;
		m_persistenceMemory.setTracer(tracer);
	}

	public IObservation step(IAct act, IStimulation[] visualStimulations,
			IStimulation[] tactileStimulations, IStimulation kinematicStimulation,
			IStimulation gustatoryStimulation) 
	{
		// Tick the clock
		m_persistenceMemory.tick();
		
		m_gustatoryStimulation = gustatoryStimulation.getValue();
		m_kinematicStimulation = kinematicStimulation.getValue();

		// Update the local space memory
		m_localSpaceMemory.update(act, kinematicStimulation);
		
		// Construct the list of saliences. 
		
		List<ISalience> saliences = new ArrayList<ISalience>();
		saliences = getSaliences(visualStimulations, tactileStimulations); 	// Saliences computed by Ernest.
		//saliences = m_salienceList; 							            // Saliences provided by VacuumSG.

		// Create new bundles and place them in the local space memory.
		
		createBundles(saliences, tactileStimulations[3].getValue() , m_kinematicStimulation, m_gustatoryStimulation);
		
		// Clean up the local space memory according to the tactile simulations.
		
		adjustLocalSpaceMemory(tactileStimulations);
		
		// Set the attractiveness of saliences.
		// TODO construct a list of places of interest whether they are visible or not. 
		
		setSaliencesAttractiveness(saliences);
		//List<ISalience> attractiveSaliences = attractiveSaliences(saliences);

		// Find the most attractive or the most repulsive salience in the list (abs value) (There is at least a wall)
		
		int maxAttractiveness = 0;
		float direction = 0;
		ISalience focusSalience = null;
		//for (ISalience salience : attractiveSaliences)
		for (ISalience salience : saliences)
		{
			if (Math.abs(salience.getAttractiveness()) > Math.abs(maxAttractiveness))
			{
				maxAttractiveness = salience.getAttractiveness();
				direction = salience.getDirection();
				focusSalience = salience;
			}
		}
		
		// Trace the focus salience and the local space memory.
		if (m_tracer != null) 
		{
			Object e = m_tracer.addEventElement("focus");
			m_tracer.addSubelement(e, "salience", focusSalience.getHexColor());
			if (focusSalience.getEvokedBundle() != null)
				focusSalience.getEvokedBundle().trace(m_tracer, "focus_bundle");
			m_localSpaceMemory.Trace(m_tracer);
		}
		
		// Return the new observation.
		
		IObservation observation = new Observation();
		observation.setGustatory(gustatoryStimulation);
		observation.setKinematic(kinematicStimulation);
		observation.setAttractiveness(maxAttractiveness);
		observation.setDirection(direction);
		
		return observation;
	}
	
	public IStimulation addStimulation(int type, int value) 
	{
		return m_persistenceMemory.addStimulation(type, value);
	}

	public int getValue(int i, int j)
	{
		if (i == 1 && j == 0 && m_kinematicStimulation == Ernest.STIMULATION_KINEMATIC_BUMP)
			return Ernest.STIMULATION_KINEMATIC_BUMP;
		else if (i == 1 && j == 1 && m_gustatoryStimulation == Ernest.STIMULATION_GUSTATORY_FISH)
			return Ernest.STIMULATION_GUSTATORY_FISH;
		else
		{
			Vector3f position = new Vector3f(1 - j, 1 - i, 0);
			return m_localSpaceMemory.getValue(position);
		}
	}

	/**
	 * Generate the list of the saliences from the sensory stimulations.
	 * @param visualStimulations The visual stimulations.
	 * @param tactileStimulations The tactile stimulations.
	 * @return The salience list.
	 */
	private List<ISalience> getSaliences(IStimulation[] visualStimulations,
				IStimulation[] tactileStimulations)
	   {
		// Visual saliences ====

		List<ISalience> saliences = new ArrayList<ISalience>(Ernest.RESOLUTION_COLLICULUS);

		IStimulation stimulation = visualStimulations[0];
		int span = 1;
		float theta = - 11 * (float)Math.PI / 24; 
		float sumDirection = theta;
		float spanf = (float)Math.PI / 12;
		for (int i = 1 ; i < Ernest.RESOLUTION_RETINA; i++)
		{
			theta += (float)Math.PI / 12;
			if (visualStimulations[i].equals(stimulation))
			{
				// measure the salience span and average direction
				span++;
                sumDirection += theta;
                spanf += (float)Math.PI / 12;
			}
			else 
			{	
				// Record the previous salience
				ISalience salience = new Salience(stimulation.getValue(), Ernest.MODALITY_VISUAL, sumDirection / span, 1, spanf);
				saliences.add(salience);
				// look for the next salience
				stimulation = visualStimulations[i];
				span = 1;
				spanf = (float)Math.PI / 12;
				sumDirection = theta;
			}
		}
		// record the last salience
		ISalience last = new Salience(stimulation.getValue(),  Ernest.MODALITY_VISUAL, sumDirection / span, 1, spanf);
		saliences.add(last);
	
		// Tactile saliences =====
		
		IStimulation tactileStimulation = tactileStimulations[0];
		span = 1;
		theta = - 3 * (float)Math.PI / 4; 
		sumDirection = theta;
		spanf = (float)Math.PI / 4;
		for (int i = 1 ; i < 7; i++)
		{
			theta += (float)Math.PI / 4;
			if (tactileStimulations[i].equals(tactileStimulation))
			{
				// measure the salience span and average direction
				span++;
                sumDirection += theta;
                spanf += (float)Math.PI / 4;
			}
			else 
			{	
				// Record the previous salience
				ISalience salience = new Salience(tactileStimulation.getValue(), Ernest.MODALITY_TACTILE, sumDirection / span, 1, spanf);
				saliences.add(salience);
				// look for the next salience
				tactileStimulation = tactileStimulations[i];
				span = 1;
				spanf = (float)Math.PI / 4;
				sumDirection = theta;
			}
		}
		// record the last salience
		ISalience salience = new Salience(tactileStimulation.getValue(),  Ernest.MODALITY_TACTILE, sumDirection / span, 1, spanf);
		saliences.add(salience);
		
	   return saliences;
   }

	/**
	 * Set the list of saliences from the list provided by VacuumSG.
	 * @param salienceList The list of saliences provided by VacuumSG.
	 */
	public void setSalienceList(ArrayList<ISalience> salienceList)
	{
		m_salienceList = salienceList;
	}
	
	/**
	 * Set the attractiveness of the saliences in the list of saliences.
	 */
	private void setSaliencesAttractiveness(List<ISalience> salienceList)
	{
		int clock = m_persistenceMemory.getClock();
		for (ISalience salience : salienceList)
		{
			if (salience.getModality() == Ernest.MODALITY_VISUAL)
			{
				// Attractiveness of visual saliences.
				IBundle b = m_persistenceMemory.seeBundle(salience.getValue());
				if (b == null)
					salience.setAttractiveness(Ernest.ATTRACTIVENESS_OF_UNKNOWN + (int)(5 * salience.getSpan() / ((float)Math.PI / 12)));
				else
				{
					salience.setAttractiveness(b.getExtrapersonalAttractiveness(clock) + (int)(5 * salience.getSpan() / ((float)Math.PI / 12)));
					salience.setEvokedBundle(b);
				}
			}
			else if (salience.isFrontal() && salience.getValue()== Ernest.STIMULATION_TOUCH_WALL)
			{
				// Attractiveness of touching a wall.
				IBundle b = m_persistenceMemory.touchBundle(salience.getValue());
				if (b == null)
					salience.setAttractiveness(Ernest.ATTRACTIVENESS_OF_HARD);
				else
				{
					//salience.setBundle(b);
					salience.setAttractiveness(b.getPeripersonalAttractiveness(clock));
					salience.setValue(b.getValue());
					salience.setEvokedBundle(b);
				}
			}
			else if (salience.getValue()== Ernest.STIMULATION_TOUCH_FISH)// != Ernest.STIMULATION_TOUCH_EMPTY.getValue())
			{
				// Attractiveness of touching a fish.
				IBundle b = m_persistenceMemory.touchBundle(salience.getValue());
				if (b != null)
				{
					//salience.setBundle(b);
					salience.setAttractiveness(b.getPeripersonalAttractiveness(clock));
					salience.setValue(b.getValue());
					salience.setEvokedBundle(b);
					// Place the bundle in the local space memory
					m_localSpaceMemory.addPlace(b, salience.getPosition());					
				}
			}
		}
		
		// Add saliences from bundles in the local space memory.
		for (IPlace place : m_localSpaceMemory.getPlaces())
		{
			IBundle b = place.getBundle();
			if (b.getGustatoryValue() == Ernest.STIMULATION_GUSTATORY_FISH)
			{
				ISalience salience = new Salience(b.getVisualValue(), Ernest.MODALITY_VISUAL, place.getPosition(), Ernest.ATTRACTIVENESS_OF_FISH - 10);
				salience.setEvokedBundle(b);
				salienceList.add(salience);
			}
		}
	}
	
	/**
	 * Create new bundles based on cooccurrences of stimulations.
	 * Place the new bundles in the local space map.
	 * @param frontVisualStimulation The visual stimulation in front of Ernest.
	 * @param frontTactileStimulation The tactile stimulation in front of Ernest.
	 * @param kinematicStimulation The kinematic stimulation.
	 * @param gustatoryStimulation The gustatory stimulation.
	 */
	private void createBundles(List<ISalience> saliences, int frontTactileValue, int kinematicValue, int gustatoryValue)
	{
		// Find the visual salience in front of Ernest.
		ISalience frontVisualSalience = null;
		for (ISalience salience : saliences)
			if (salience.isFrontal() && salience.getModality() == Ernest.MODALITY_VISUAL && salience.getSpan() > Math.PI/6 + 0.01f )
				frontVisualSalience = salience;
		
		// Associate the tactile stimulation with the kinematic stimulation.

		IBundle frontBundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD);
		IBundle hereBundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_HERE);
		
		if (kinematicValue == Ernest.STIMULATION_KINEMATIC_BUMP)
		{
			if (frontBundle == null)
			{
				if (frontTactileValue == Ernest.STIMULATION_TOUCH_WALL)
				{
					if (frontVisualSalience == null)
					{
						IBundle b = m_persistenceMemory.createTactoKinematicBundle(frontTactileValue, Ernest.STIMULATION_KINEMATIC_BUMP);
						m_localSpaceMemory.addPlace(b, LocalSpaceMemory.DIRECTION_AHEAD);
					}
					else
					{
						IBundle b = m_persistenceMemory.addBundle(frontVisualSalience.getValue(), frontTactileValue, Ernest.STIMULATION_KINEMATIC_BUMP, Ernest.STIMULATION_GUSTATORY_NOTHING);
						m_localSpaceMemory.addPlace(b, LocalSpaceMemory.DIRECTION_AHEAD);
					}
				}
			}
			else if (frontBundle.getTactileValue() == Ernest.STIMULATION_TOUCH_WALL)
				m_persistenceMemory.addKinematicValue(frontBundle, kinematicValue);
		}

		// Associate the tactile stimulation with the gustatory stimulation
		
		if (gustatoryValue == Ernest.STIMULATION_GUSTATORY_FISH)
		{
			// Discrete environment. The fish bundle is the hereBundle.
			if (hereBundle == null)
			{
				m_persistenceMemory.createTactoGustatoryBundle(Ernest.STIMULATION_TOUCH_FISH, gustatoryValue);
			}
			else if (hereBundle.getTactileValue() == Ernest.STIMULATION_TOUCH_FISH)
			{
				m_persistenceMemory.addGustatoryValue(hereBundle, gustatoryValue);
				m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_HERE); // The fish is eaten
			}
			
			// Continuous environment. The fish bundle is the frontBundle
			if (frontTactileValue == Ernest.STIMULATION_TOUCH_FISH)
			{
				if (frontBundle == null) // Continuous environment. 
				{
					IBundle bundle = m_persistenceMemory.createTactoGustatoryBundle(frontTactileValue, gustatoryValue);
					if (frontVisualSalience != null )
						m_persistenceMemory.addVisualValue(bundle, frontVisualSalience.getValue());
				}
				else if (frontBundle.getTactileValue() == frontTactileValue)
				{
					m_persistenceMemory.addGustatoryValue(frontBundle, gustatoryValue);
					m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD); // The fish is eaten
				}
			}
		}
		
		// Associate the tactile stimulation with the social stimulation
		
		if (gustatoryValue == Ernest.STIMULATION_SOCIAL_CUDDLE && frontTactileValue == Ernest.STIMULATION_TOUCH_AGENT)
		{
			if (frontBundle == null) // Continuous environment. 
			{
				IBundle bundle = m_persistenceMemory.createTactoGustatoryBundle(frontTactileValue, gustatoryValue);
				if (frontVisualSalience != null )
					m_persistenceMemory.addVisualValue(bundle, frontVisualSalience.getValue());
				m_localSpaceMemory.addPlace(bundle, LocalSpaceMemory.DIRECTION_AHEAD);
			}
			else if (frontBundle.getTactileValue() == frontTactileValue)
			{
				m_persistenceMemory.addGustatoryValue(frontBundle, gustatoryValue);
			}
		}
		
		// Associate the visual stimulation with the tactile stimulation.
		
		if (frontVisualSalience != null )
		{
			if (frontBundle == null)
			{
				if (frontTactileValue != Ernest.STIMULATION_TOUCH_EMPTY)		
				{
					IBundle bundle = m_persistenceMemory.createVisioTactileBundle(frontVisualSalience.getValue(), frontTactileValue);
					m_localSpaceMemory.addPlace(bundle, LocalSpaceMemory.DIRECTION_AHEAD);
				}
			}
			else
			{
				if (frontTactileValue == frontBundle.getTactileValue())
				{
					m_persistenceMemory.addVisualValue(frontBundle, frontVisualSalience.getValue());
				}
			}
		}
	}
	
	/**
	 * Remove the bundles in local space memory that are not consistent with the tactile stimuli.
     * TODO The criteria to decide whether the matching is correct or incorrect need to be learned ! 
	 * @param tactileStimulations The tactile stimuli.
	 */
	private void adjustLocalSpaceMemory(IStimulation[] tactileStimulations)
	{

		// Check right
		IBundle bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_RIGHT);
//		if (bundle != null && bundle.getTactileValue() != tactileStimulations[1].getValue())
//			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_RIGHT);
//
//		// Check ahead right
//		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD_RIGHT);
//		if (bundle != null && bundle.getTactileValue() != tactileStimulations[2].getValue())
//			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD_RIGHT);

		// Check ahead 
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[3].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD);

		// Check ahead left
//		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_AHEAD_LEFT);
//		if (bundle != null && bundle.getTactileValue() != tactileStimulations[4].getValue())
//			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_AHEAD_LEFT);
//
//		// Check left
//		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_LEFT);
//		if (bundle != null && bundle.getTactileValue() != tactileStimulations[5].getValue())
//			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_LEFT);

		// Check here
		bundle = m_localSpaceMemory.getBundle(LocalSpaceMemory.DIRECTION_HERE);
		if (bundle != null && bundle.getTactileValue() != tactileStimulations[8].getValue())
			m_localSpaceMemory.clearPlace(LocalSpaceMemory.DIRECTION_HERE);

	}
}
