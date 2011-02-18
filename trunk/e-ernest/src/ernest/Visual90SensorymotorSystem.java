package ernest;

import java.awt.Color;

/**
 * Implement Ernest 9.0's sensorymotor system. 
 * Recieves a matrix of EyeFixation objects from the environment.
 * @author ogeorgeon
 */
public class Visual90SensorymotorSystem  extends BinarySensorymotorSystem
{

	/** The values of the pixels */
	private int m_currentLeftDistance   = Ernest.INFINITE;
	private int m_currentRightDistance  = Ernest.INFINITE;
	private int m_previousLeftDistance  = Ernest.INFINITE;
	private int m_previousRightDistance = Ernest.INFINITE;
	
	private ILandmark m_currentLeftLandmark;
	private ILandmark m_currentRightLandmark;
	
	/** The features that are sensed by the distal system. */
	private String m_leftFeature = " ";
	private String m_rightFeature = " ";
	
	/** The intrinsic satisfaction of sensing the current features */
	private int m_satisfaction = 0;
	
	private int PROXIMITY_DISTANCE = 2;
	
	/** The taste of water */
	private int TASTE_WATER = 1;

	/** The taste of glucose */
	private int TASTE_FOOD = 2;

	/**
	 * Determine the enacted act.
	 * Made from the binary feedback status plus the features provided by the distal sensory system. 
	 * @param schema The selected schema. 
	 * @param status The binary feedback
	 */
	public IAct enactedAct(ISchema schema, boolean status)
	{
		// The schema is null during the first cycle, then the enacted act is null.
		if (schema == null) return null;
		
		// Computes the act's label from the features returned by the sensory system and from the status.
		
		String label = schema.getLabel();
		
		if (!m_leftFeature.equals(" ") || !m_rightFeature.equals(" ") )
			label = label + "" + m_leftFeature + "|" + m_rightFeature;
		if (status)
			label = "(" + label + ")";
		else 
			label = "[" + label + "]";
		
		// Bump into a landmark
		if (label.equals("[>]"))
			m_attentionalSystem.bump(m_currentLeftLandmark);
		
		// Compute the act's satisfaction 
		
		int satisfaction = m_satisfaction + schema.resultingAct(status).getSatisfaction();  
		
		// Create the act in episodic memory if it does not exist.
		
		IAct enactedAct = m_episodicMemory.addAct(label, schema, status, satisfaction, Ernest.RELIABLE);
		
		return enactedAct;
	}
	
	/**
	 * Generate sensory features from the sensed matrix sent by the environment.
	 * @param matrix The matrix sensed in the environment. 
	 */
	public void senseMatrix(int[][] matrix) 
	{
		// Vision =====
		
		m_previousLeftDistance  = m_currentLeftDistance;
		m_previousRightDistance = m_currentRightDistance;
		m_currentLeftDistance   = matrix[0][0];
		m_currentRightDistance  = matrix[1][0];
		
		m_currentLeftLandmark   = m_episodicMemory.addLandmark(matrix[0][1], matrix[0][2], matrix[0][3]);
		m_currentRightLandmark  = m_episodicMemory.addLandmark(matrix[1][1], matrix[1][2], matrix[1][3]);
		
		if (m_currentLeftDistance <= PROXIMITY_DISTANCE)
			m_attentionalSystem.check(m_currentLeftLandmark);
		if (m_currentRightDistance <= PROXIMITY_DISTANCE)
			m_attentionalSystem.check(m_currentRightLandmark);
		
		// Landmarks are inhibited if they have been recently checked 
		// and they don't satisfy Ernest's current homeostatic state, 
		// or if they are regular wall.
		if (m_attentionalSystem.isInhibited(m_currentLeftLandmark))
			m_currentLeftDistance = Ernest.INFINITE;
		if (m_attentionalSystem.isInhibited(m_currentRightLandmark))
			m_currentRightDistance = Ernest.INFINITE;
			
		
/*		// If Ernest has a goal landmark then the visual system is blind to other landmarks.
		if ( (m_attentionalSystem.getGoalLandmark() != null) && !m_attentionalSystem.getGoalLandmark().equals(m_currentLeftLandmark))
		{
			m_currentLeftDistance = Ernest.INFINITE;
		}
		if ((m_attentionalSystem.getGoalLandmark() != null) && !m_attentionalSystem.getGoalLandmark().equals(m_currentRightLandmark))
		{
			m_currentRightDistance = Ernest.INFINITE;
		}
*/		
		m_satisfaction = 0;
		
		// The sensed features correspond to changes in the pixels.
		m_leftFeature  = sensePixel(m_previousLeftDistance, m_currentLeftDistance);
		m_rightFeature = sensePixel(m_previousRightDistance, m_currentRightDistance);		
		
		if (m_leftFeature.equals("o") && m_rightFeature.equals("o"))
			m_satisfaction = -100;
		
		// Taste =====
		
		int taste = matrix[2][0];
		
		if (taste == TASTE_WATER)
			m_attentionalSystem.drink(m_currentLeftLandmark);
		if (taste == TASTE_FOOD)
			m_attentionalSystem.eat(m_currentLeftLandmark);
		
	}

	/**
	 * Sense the feature based on a pixel change 
	 * @param previousPixel The pixel's previous value.
	 * @param currentPixel The pixel's current value.
	 * @return The sensed feature
	 */
	private String sensePixel(int previousPixel, int currentPixel) 
	{
		String feature = " ";
		int satisfaction = 0;
		
		// arrived
		if (previousPixel > currentPixel && currentPixel == 0)
		{
			feature = "x";
			satisfaction = 100;
		}
		
		// closer
		else if (previousPixel < Ernest.INFINITE && currentPixel < previousPixel)
		{
			feature = "+";
			satisfaction = 200;
		}

		// appear
		else if (previousPixel == Ernest.INFINITE && currentPixel < Ernest.INFINITE)
		{
			feature = "*";
			satisfaction = 50;
		}
		
		// disappear
		else if (previousPixel < Ernest.INFINITE && currentPixel == Ernest.INFINITE)
		{
			feature = "o";
			satisfaction = -100;
		}

		System.out.println("Sensed " + feature);
		
		m_satisfaction += satisfaction;

		return feature;
	}
	
}