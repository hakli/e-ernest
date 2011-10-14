package imos;

import ernest.ITracer;

/**
 * The Intrinsically Motivated Schema mechanism.
 * @author ogeorgeon
 */
public interface IImos 
{

	/**
	 * The main method of the Intrinsic Motivation System.
	 * Follow-up the sequence at hand, and chooses the next primitive interaction to try to enact. 
	 * @param primitiveEnaction The last actually enacted primitive interaction.
	 * @return The primitive interaction to try to enact next. 
	 */
	public IAct step(IAct primitiveEnaction); 

	/**
	 * Create a primitive act and a primitive schema.
	 * @param actionLabel The schema's label that is interpreted by the environment.
	 * @param status The act's succeed or fail status 
	 * @param satisfaction The act's satisfaction 
	 * @return the created primitive act.
	 */
	public IAct addPrimitiveAct(String actionLabel, boolean status, int satisfaction); 
	
	/**
	 * Constructs a new interaction in episodic memory.
	 * or retrieve the the interaction if it already exists.
	 * The interaction's action is recorded as a primitive schema.
	 * If there is no stimuli, the interaction is marked as the schema's succeeding or failing interaction.
	 * @param actionLabel The label of the interaction's action in the environment.
	 * @param stimuliLabel The label of the interaction's stimuli generated by the environment.
	 * @param satisfaction The interaction's satisfaction.
	 * @return The act that was created or that already existed.
	 */
	public IAct addInteraction(String actionLabel, String stimuliLabel, int satisfaction); 

	/**
	 * @param tracer The tracer used to generate the activity traces
	 */
	public void setTracer(ITracer<Object> tracer);
	
	/**
	 * Get a description of the agent's internal state. (for visualization in the environment).
	 * @return A representation of Ernest's internal state
	 */
	public String getInternalState();
	
}
