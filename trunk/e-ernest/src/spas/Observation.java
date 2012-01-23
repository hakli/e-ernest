package spas;

import javax.vecmath.Vector3f;

import ernest.Ernest;

/**
 * An observation holds the significant consequences that the enacted interaction had on the spatial system.
 * It is the structure that supports the interaction between the spatial system (spas) 
 * and the intrinsic motivation system (imos).
 * @author Olivier
 */
public class Observation implements IObservation 
{

	private float m_span = 0;

	private Vector3f m_position = new Vector3f();
	
	private Vector3f m_speed = new Vector3f();
	
	/** The attractiveness of Ernest's interest. */
	private int m_attractiveness = 0;

	/** The kinematic stimulation. */
	private IStimulation m_kinematicStimulation;
	
	/** The gustatory stimulation. */
	private IStimulation m_gustatoryStimulation;
	
	/** The focus bundle. */
	private IBundle m_bundle; 
	
	/** The initial feedback obtained when the act starts. */
	private String m_initialFeedback = "";
	
	/** The resulting stimuli of the enacted act. */
	private String m_stimuli;

	public void setAttractiveness(int attractiveness) 
	{
		m_attractiveness = attractiveness;
	}

	public int getAttractiveness() 
	{
		return m_attractiveness;
	}

	public void setKinematic(IStimulation kinematicStimulation)
	{
		m_kinematicStimulation = kinematicStimulation;
	}

	public IStimulation getKinematicStimulation()
	{
		return m_kinematicStimulation;
	}

	public void setGustatory(IStimulation gustatoryStimulation)
	{
		m_gustatoryStimulation = gustatoryStimulation;
	}
	
	public IStimulation getGustatoryStimulation()
	{
		return m_gustatoryStimulation;
	}

	public void setPosition(Vector3f position) 
	{
		m_position.set(position);
	}

	public Vector3f getPosition() 
	{
		return m_position;
	}

	public float getDirection() 
	{
		return (float)Math.atan2((double)m_position.y, (double)m_position.x);
	}

	public float getDistance() 
	{
		return m_position.length();
	}

	public void setSpan(float span) 
	{
		m_span = span;
	}

	public float getSpan() 
	{
		return m_span;
	}

	public void setBundle(IBundle bundle) 
	{
		m_bundle = bundle;
	}

	public IBundle getBundle() 
	{
		return m_bundle;
	}

	public void setSpeed(Vector3f speed) 
	{
		m_speed = speed;
	}

	public Vector3f getSpeed()
	{
		return m_speed;
	}

	public void setInitialFeedback(String initialFeedback) 
	{
		m_initialFeedback = initialFeedback;
	}

	public void setStimuli(String stimuli) 
	{
		m_stimuli = stimuli;
	}

	public String getInitialFeedback() 
	{
		return m_initialFeedback;
	}

	public String getStimuli() 
	{
		return m_stimuli;
	}
}