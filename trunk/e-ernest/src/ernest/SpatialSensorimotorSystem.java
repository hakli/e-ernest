package ernest;

import javax.vecmath.Vector3f;

import imos.IAct;

import org.w3c.dom.Element;

import spas.IObservation;
import spas.IStimulation;
import spas.LocalSpaceMemory;
import spas.Observation;
import spas.Stimulation;
import utils.ErnestUtils;

/**
 * Implement Ernest 10.0's sensorimotor system.
 * Ernest 10.0 has a visual resolution of 2x12 pixels and a kinesthetic resolution of 3x3 pixels.
 * @author ogeorgeon
 */
public class SpatialSensorimotorSystem  extends BinarySensorymotorSystem
{
	/** The current observation generated by the spatial system */
	private IObservation m_observation = null;
	
	private String m_visualStimuli = "";
	private String m_stimuli = "";
	private int m_satisfaction = 0;
	private boolean m_status;
	private IAct m_primitiveAct = null;
	private float m_rotation = 0;
	
	private IStimulation[] m_visualStimulations;
	private IStimulation[] m_tactileStimulations;
	private IStimulation m_kinematicStimulation;
	private IStimulation m_gustatoryStimulation;

	final static float TRANSLATION_IMPULSION = .15f; // .13f
	final static float ROTATION_IMPULSION = 0.123f;//(float)Math.toRadians(7f); // degrees   . 5.5f
	
	private int m_interactionTimer = 0;
	private IObservation m_initialObservation = null;

	public int[] update(int[][] stimuli) 
	{
		m_interactionTimer++;
		
		int primitiveSchema[] = new int[2];
		float translationx = (float) stimuli[2][8] / Ernest.INT_FACTOR; 
		float translationy = (float) stimuli[3][8] / Ernest.INT_FACTOR;
		m_rotation = (float) stimuli[4][8] / Ernest.INT_FACTOR;
		float speed = (float) stimuli[5][8] / Ernest.INT_FACTOR;
		int cognitiveMode = stimuli[6][8];

		// Update the local space memory
    	m_spas.update(new Vector3f(-translationx, -translationy, 0), - m_rotation);

    	// Sense the environment
    	sense(stimuli);
    	
		// Generate a spatial observation ====
		
		//IObservation newObservation = m_spas.step(m_visualStimulations, m_tactileStimulations, m_kinematicStimulation, m_gustatoryStimulation);		
		IObservation newObservation = m_spas.step(m_tactileStimulations, m_kinematicStimulation, m_gustatoryStimulation);
		
    	// Update the feedback.
		if (m_observation != null)
		{
			setInitialFeedback(m_observation, newObservation);
			setDynamicFeature(m_observation, newObservation);
		}
		else
			newObservation.setSpeed(new Vector3f());

    	// Record the initial observation.
    	if (m_interactionTimer == 1)
    	{
    		m_initialObservation = newObservation;
    	}
    		
		m_observation = newObservation;

    	primitiveSchema[0] = 0;
    	primitiveSchema[1] = 0;
    	
    	// Trigger a new cognitive loop when the speed is below a threshold.
        //if ((speed <= .050f) && (Math.abs(m_rotation) <= .050f) && cognitiveMode > 0)
        if (actEnd(speed) && cognitiveMode > 0)	
        //if ((m_observation.getSpeed().length() <= .050f)  && cognitiveMode > 0)
        {
        	m_spas.tick(); // Tick the clock of persistence memory
    		m_interactionTimer = 0;
    		        	
    		IAct enactedPrimitiveAct = null;
 
    		if (m_tracer != null)
    		{
                m_tracer.startNewEvent(m_imos.getCounter());
    			m_tracer.addEventElement("clock", m_imos.getCounter() + "");
    		}                

    		// If the intended act was null (during the first cycle), then the enacted act is null.

    		// Compute the enacted act == 
    		
    		if (m_primitiveAct != null)
    		{
    			if (m_primitiveAct.getSchema().getLabel().equals(">"))
    				m_satisfaction = m_satisfaction + (m_status ? 20 : -100);
    			else
    				m_satisfaction = m_satisfaction + (m_status ? -10 : -20);
    		
    			enactedPrimitiveAct = m_imos.addInteraction(m_primitiveAct.getSchema().getLabel(), m_stimuli, m_satisfaction);

    			if (m_tracer != null) 
    				m_tracer.addEventElement("primitive_enacted_schema", m_primitiveAct.getSchema().getLabel());
    		}
    		
    		// Let Ernest decide for the next primitive schema to enact.
    		
    		m_primitiveAct = m_imos.step(enactedPrimitiveAct);
    		primitiveSchema[0] = m_primitiveAct.getSchema().getLabel().toCharArray()[0];
    		
    		// Once the decision is made, compute the intensity.
    		
    		primitiveSchema[1] = impulsion(primitiveSchema[0]);
    		
    		if (m_tracer != null)
    		{
    			// Observation
    			Object e = m_tracer.addEventElement("current_observation");
    			m_tracer.addSubelement(e, "direction", m_observation.getDirection() + "");
    			m_tracer.addSubelement(e, "distance", m_observation.getDistance() + "");
    			m_tracer.addSubelement(e, "span", m_observation.getSpan() + "");
    			m_tracer.addSubelement(e, "attractiveness", m_observation.getAttractiveness() + "");
    			m_tracer.addSubelement(e, "relative_speed_x", m_observation.getSpeed().x + "");
    			m_tracer.addSubelement(e, "relative_speed_y", m_observation.getSpeed().y + "");
    			m_tracer.addSubelement(e, "stimuli", m_stimuli);
    			m_tracer.addSubelement(e, "dynamic_feature", m_visualStimuli);
    			m_tracer.addSubelement(e, "satisfaction", m_satisfaction + "");
    			m_tracer.addSubelement(e, "kinematic", m_observation.getKinematicStimulation().getHexColor());
    			m_tracer.addSubelement(e, "gustatory", m_observation.getGustatoryStimulation().getHexColor());
    			
    			Object focusElmt = m_tracer.addEventElement("focus");
    			m_tracer.addSubelement(focusElmt, "salience", ErnestUtils.hexColor(m_observation.getBundle().getValue()));
    			m_observation.getBundle().trace(m_tracer, "focus_bundle");

    			// Vision
				Object retinaElmt = m_tracer.addEventElement("retina");
				for (int i = Ernest.RESOLUTION_RETINA - 1; i >= 0 ; i--)
					m_tracer.addSubelement(retinaElmt, "pixel_0_" + i, m_visualStimulations[i].getHexColor());

				// Tactile
    			Object s = m_tracer.addEventElement("tactile");
    			m_tracer.addSubelement(s, "here", m_tactileStimulations[8].getHexColor());
    			m_tracer.addSubelement(s, "rear", m_tactileStimulations[7].getHexColor());
    			m_tracer.addSubelement(s, "touch_6", m_tactileStimulations[6].getHexColor());
    			m_tracer.addSubelement(s, "touch_5", m_tactileStimulations[5].getHexColor());
    			m_tracer.addSubelement(s, "touch_4", m_tactileStimulations[4].getHexColor());
    			m_tracer.addSubelement(s, "touch_3", m_tactileStimulations[3].getHexColor());
    			m_tracer.addSubelement(s, "touch_2", m_tactileStimulations[2].getHexColor());
    			m_tracer.addSubelement(s, "touch_1", m_tactileStimulations[1].getHexColor());
    			m_tracer.addSubelement(s, "touch_0", m_tactileStimulations[0].getHexColor());
    			
    			// Local space memory
    			
    			m_spas.traceLocalSpace();
    		}
        }

		return primitiveSchema;    		
	}
	
	private boolean actEnd(float speed)
	{
		boolean actEnd = false;
		
        //if ((m_observation.getSpeed().length() <= .050f) && (Math.abs(m_rotation) <= .050f))
        if ((speed <= .050f) && (Math.abs(m_rotation) <= .050f))
        	actEnd = true;
        
        // If the act does not start as intended, then end and return the actually enacted act
        
        // If the act does start as intended, then end when the act is no more as intended and returns the intended act. 
        
    	return actEnd;
	}
	
	public IAct enactedAct(IAct act, int[][] stimuli) 
	{
		// If the intended act was null (during the first cycle), then the enacted act is null.
		IAct enactedAct = null;		

		// Sense the environment ===
		
		sense(stimuli);
		
		// Compute the enacted act == 
		
		if (act != null)
		{
			if (act.getSchema().getLabel().equals(">"))
				m_satisfaction = m_satisfaction + (m_status ? 20 : -100);
			else
				m_satisfaction = m_satisfaction + (m_status ? -10 : -20);
		
			enactedAct = m_imos.addInteraction(act.getSchema().getLabel(), m_stimuli, m_satisfaction);

			if (m_tracer != null) 
				m_tracer.addEventElement("primitive_enacted_schema", act.getSchema().getLabel());
		}
		return enactedAct;
	}
	
	public void sense(int[][] stimuli)
	{
		// Vision =====
		
		m_visualStimulations = new Stimulation[Ernest.RESOLUTION_RETINA];
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++)
		{
			m_visualStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_VISUAL, stimuli[i][1] * 65536 + stimuli[i][2] * 256 + stimuli[i][3]);
			float angle = (float) (- 11 * Math.PI/24 + i * Math.PI/12); 
			Vector3f pos = new Vector3f((float) Math.cos(angle) * stimuli[i][0] / Ernest.INT_FACTOR, (float) Math.sin(angle) * stimuli[i][0] / Ernest.INT_FACTOR, 0);
			m_visualStimulations[i].setPosition(pos);
		}
		
		// Touch =====
		
		m_tactileStimulations = new IStimulation[9];
		
		for (int i = 0; i < 9; i++)
			m_tactileStimulations[i] = m_spas.addStimulation(Ernest.MODALITY_TACTILE, stimuli[i][9]);
			
		// Kinematic ====
		
		//IStimulation kinematicStimulation;
		
		m_kinematicStimulation = m_spas.addStimulation(Ernest.STIMULATION_KINEMATIC, stimuli[1][8]);

		// Taste =====
		
		//IStimulation 
		m_gustatoryStimulation = m_spas.addStimulation(Ernest.STIMULATION_GUSTATORY, stimuli[0][8]); 

	}
	
	/**
	 * Generate the dynamic stimuli from the impact in the local space memory.
	 * The stimuli come from: 
	 * - The kinematic feature.
	 * - The variation in attractiveness and in direction of the object of interest. 
	 * @param act The enacted act.
	 */
	private void setDynamicFeature(IObservation previousObservation, IObservation newObservation)
	{
		int   newAttractiveness = newObservation.getAttractiveness();
		float newDirection = newObservation.getDirection();
		int   previousAttractiveness = previousObservation.getAttractiveness();
		float previousDirection = previousObservation.getDirection();
		Vector3f relativeSpeed = new Vector3f();
		
		if (newObservation.getSpeed() == null)
		{
			relativeSpeed.set(newObservation.getPosition());
			relativeSpeed.sub(previousObservation.getPosition());
			
			Vector3f rotationSpeed = new Vector3f(- newObservation.getPosition().y, newObservation.getPosition().x, 0); // Orthogonal to the position vector.
			rotationSpeed.normalize();
			rotationSpeed.scale(- m_rotation);
			relativeSpeed.add(rotationSpeed);
			newObservation.setSpeed(relativeSpeed);
		}
		else 
			relativeSpeed.set(newObservation.getSpeed());
		
		String dynamicFeature = "";
		
		float minFovea =  - (float)Math.PI / 4 + 0.01f;
		float maxFovea =    (float)Math.PI / 4 - 0.01f;
		
		int satisfaction = 0;

		if (newAttractiveness >= 0)
		{
			// Positive attractiveness
			{
				// Attractiveness
				//if (previousAttractiveness > newAttractiveness)
				if (relativeSpeed.dot(newObservation.getPosition()) > 0)
					// Farther
					dynamicFeature = "-";
				//else if (previousAttractiveness < newAttractiveness)
				else if (relativeSpeed.dot(newObservation.getPosition()) < 0)
					// Closer
					dynamicFeature = "+";
				//else if (Math.abs(previousDirection) < Math.abs(newDirection))
				else if (relativeSpeed.y * newDirection > 0)
					// More outward (or same direction, therefore another salience)
					dynamicFeature = "-";
				//else if (Math.abs(previousDirection) > Math.abs(newDirection))
				else if (relativeSpeed.y * newDirection < 0)
					// More inward
					dynamicFeature = "+";
				
				dynamicFeature = m_initialObservation.getInitialFeedback();
		
				if (dynamicFeature.equals("-"))
					satisfaction = -100;
				if (dynamicFeature.equals("+"))
					satisfaction = 20;
	
				// Direction
				
				if (!dynamicFeature.equals(""))
				{
					if (newDirection <= minFovea)
						dynamicFeature = "|" + dynamicFeature;
					else if (newDirection >= maxFovea )
						dynamicFeature = dynamicFeature + "|";
				}		
			}
		}
		else
		{
			// Negative attractiveness (repulsion)
			
			// Variation in attractiveness
			if (previousAttractiveness >= 0)
				// A wall appeared with a part of it in front of Ernest
				dynamicFeature = "*";		
			//else if (Math.abs(previousDirection) < Math.abs(newDirection))
			else if (relativeSpeed.y * newDirection > 0)
				// The wall went more outward (Ernest closer to the edge)
				dynamicFeature = "_";
			//else if (Math.abs(previousDirection) > Math.abs(newDirection))
			else if (relativeSpeed.y * newDirection < 0)
				// The wall went more inward (Ernest farther to the edge)
				dynamicFeature = "*";
	
			dynamicFeature = m_initialObservation.getInitialFeedback();

			if (dynamicFeature.equals("*"))
				satisfaction = -100;
			if (dynamicFeature.equals("_"))
				satisfaction = 20;
			
			// Direction feature
			
			if (!dynamicFeature.equals(""))
			{
				if (newDirection < -0.1f ) 
					dynamicFeature = "|" + dynamicFeature;
				else if (newDirection > 0.1f )
					dynamicFeature = dynamicFeature + "|";
			}		
		}
		
		// Gustatory
		
		if (newObservation.getGustatoryStimulation().getValue() != Ernest.STIMULATION_GUSTATORY_NOTHING)
		{
			dynamicFeature = "e";
			satisfaction = 100;
		}
		
		m_visualStimuli = dynamicFeature;
		
		// Kinematic
		
		m_status = (newObservation.getKinematicStimulation().getValue() != Ernest.STIMULATION_KINEMATIC_BUMP);
		
		m_stimuli = (m_status ? " " : "w") + dynamicFeature;

		m_satisfaction = satisfaction;
	}
	
	/**
	 * Generate the initial feedback when a new interaction is started.
	 * @param previousObservation The observation made on the previous update.
	 * @param newObservation The current observation .
	 */
	private String setInitialFeedback(IObservation previousObservation, IObservation newObservation)
	{
		String initialFeedback = "";
		
		int   newAttractiveness = newObservation.getAttractiveness();
		float newDirection = newObservation.getDirection();
		int   previousAttractiveness = previousObservation.getAttractiveness();
		Vector3f relativeSpeed = new Vector3f();
		
		if (newObservation.getSpeed() == null)
		{
			relativeSpeed.set(newObservation.getPosition());
			relativeSpeed.sub(previousObservation.getPosition());
			
			Vector3f rotationSpeed = new Vector3f(- newObservation.getPosition().y, newObservation.getPosition().x, 0); // Orthogonal to the position vector.
			rotationSpeed.normalize();
			rotationSpeed.scale(- m_rotation);
			relativeSpeed.add(rotationSpeed);
			newObservation.setSpeed(relativeSpeed);
		}
		else 
			relativeSpeed.set(newObservation.getSpeed());
		
		if (newAttractiveness >= 0)
		{
			// Positive attractiveness
			// Move forward
			if (m_primitiveAct.getSchema().getLabel().equals(">"))
			{
				if (relativeSpeed.dot(newObservation.getPosition()) < 0 &&
					Math.abs(newDirection) < Math.PI/4)
					// Closer
					initialFeedback = "+";
				else
					// Farther
					initialFeedback = "-";
			}
			else
			{
				if (relativeSpeed.x > 0)
					// More frontwards
					initialFeedback = "+";
				else
					// More backward
					initialFeedback = "-";
			}
//			if (relativeSpeed.dot(newObservation.getPosition()) > 0)
//				// Farther
//				initialFeedback = "-";
//			else if (relativeSpeed.dot(newObservation.getPosition()) < 0)
//				// Closer
//				initialFeedback = "+";
//			else if (relativeSpeed.y * newDirection > 0)
//				// More outward (or same direction, therefore another salience)
//				initialFeedback = "-";
//			else if (relativeSpeed.y * newDirection < 0)
//				// More inward
//				initialFeedback = "+";
		}
		else
		{
			// Negative attractiveness (repulsion)
			
			if (previousAttractiveness >= 0)
				// A wall appeared with a part of it in front of Ernest
				initialFeedback = "*";		
			if (m_primitiveAct.getSchema().getLabel().equals(">"))
			{
				if (relativeSpeed.dot(newObservation.getPosition()) < 0 &&
					Math.abs(newDirection) < Math.PI/4)
					// Closer
					initialFeedback = "*";
				else
					// Farther
					initialFeedback = "_";
			}
			else
			{
				if (relativeSpeed.x > 0)
					// More frontwards
					initialFeedback = "*";
				else
					// More backward
					initialFeedback = "_";
			}
//			else if (relativeSpeed.y * newDirection > 0)
//				// The wall went more outward (Ernest closer to the edge)
//				initialFeedback = "_";
//			else if (relativeSpeed.y * newDirection < 0)
//				// The wall went more inward (Ernest farther to the edge)
//				initialFeedback = "*";
		}	
		
		newObservation.setInitialFeedback(initialFeedback);
		return initialFeedback;
	}
	
	public int impulsion(int intentionSchema) 
	{
		int impulsion = 0;
		
		if (intentionSchema == '>')
		{
			impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR);
//			if (m_observation.getDistance() < .5f)
//				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * .5f);
//			if (m_observation.getDistance() < 1.1f)
//				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * m_observation.getDistance());
//			else
//				impulsion = (int)(TRANSLATION_IMPULSION * Ernest.INT_FACTOR * 1.1f);
		}
		if (intentionSchema == '^' || intentionSchema == 'v' )
		{ 
			impulsion = (int)(ROTATION_IMPULSION * Ernest.INT_FACTOR);
//			if (Math.abs(m_observation.getDirection()) > Math.PI/8)
//				impulsion = (int)(ROTATION_IMPULSION * Ernest.INT_FACTOR * Math.abs(m_observation.getDirection()) / (Math.PI/4));
//			if (impulsion > 2 * ROTATION_IMPULSION * Ernest.INT_FACTOR)
//				impulsion = (int)(2 * ROTATION_IMPULSION * Ernest.INT_FACTOR);
			
			//return (int)(Math.abs(m_observation.getPlace().getDirection()) * 1000);
		}
		return impulsion;
	}
}