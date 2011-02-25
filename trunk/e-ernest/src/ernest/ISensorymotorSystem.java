package ernest;

/**
 * Generic interface for Ernest's sensorymotor system
 * Support primitive binary interactions.
 * Determine the enacted act from the selected schema and the binary feedback status.  
 * @author ogeorgeon
 */
public interface ISensorymotorSystem 
{
	
	/**
	 * Initialize the sensorymotor system with the connection to episodic memory and to the attentional system
	 * @param episodicMemory Ernest's episodic memory
	 * @param attentionalSystem Ernest's attentional system
	 */
	public void init(EpisodicMemory episodicMemory, IAttentionalSystem attentionalSystem , ITracer tracer);

	/**
	 * Used by the environment to set the primitive binary sensorymotor acts.
	 * @param schemaLabel The schema's label that is interpreted by the environment.
	 * @param status The act's succeed or fail status 
	 * @param satisfaction The act's satisfaction 
	 * @return the created primitive act
	 */
	public IAct addPrimitiveAct(String schemaLabel, boolean status, int satisfaction); 

	/**
	 * Determine the enacted act 
	 * @param schema The enacted primitive schema
	 * @param status The status returned as a feedback from the enacted schema
	 * @return The enacted act
	 */
	public IAct enactedAct(ISchema schema, boolean status);
	
	/**
	 * Generate sensory features from the sensed matrix sent by the environment.
	 * (Only used by visual sensorymotor systems)
	 * @param matrix The matrix sensed in the environment. 
	 */
	public void senseMatrix(int[][] matrix); 
}
