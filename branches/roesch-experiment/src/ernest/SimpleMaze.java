package ernest;

/**
 * This class implements the Small Loop Environment
 *  
 * The Small Loop Problem: A challenge for artificial emergent cognition. 
 * Olivier L. Georgeon, James B. Marshall. 
 * BICA2012, Annual Conference on Biologically Inspired Cognitive Architectures. 
 * Palermo, Italy. (October 31, 2012).
 * http://e-ernest.blogspot.fr/2012/05/challenge-emergent-cognition.html
 *   
 * @author mcohen
 * @author ogeorgeon
 */
public class SimpleMaze implements IEnvironment 
{
	private static final int ORIENTATION_UP    = 0;
	private static final int ORIENTATION_RIGHT = 1;
	private static final int ORIENTATION_DOWN  = 2;
	private static final int ORIENTATION_LEFT  = 3;

	// The Small Loop Environment
	
	private static final int WIDTH = 6;	
	private static final int HEIGHT = 6;	
	private int m_x = 4;
	private int m_y = 1;
	private int m_o = 2;
	
	private char[][] m_board = 
		{
		 {'x', 'x', 'x', 'x', 'x', 'x'},
		 {'x', ' ', ' ', ' ', ' ', 'x'},
		 {'x', ' ', 'x', 'x', ' ', 'x'},
		 {'x', ' ', ' ', 'x', ' ', 'x'},
		 {'x', 'x', ' ', ' ', ' ', 'x'},
		 {'x', 'x', 'x', 'x', 'x', 'x'},
		};
	
//  This is the Simple Maze environment presented here: 	
//	http://e-ernest.blogspot.com/2010/12/java-ernest-72-in-vacuum.html
//	
//	private static final int WIDTH = 9;	
//	private static final int HEIGHT = 8;	
//	private int m_x = 3;
//	private int m_y = 5;
//	private int m_o = 0;
//	
//	private char[][] m_board = 
//		{
//		 {'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x'},
//		 {'x', ' ', ' ', ' ', 'x', 'x', 'x', 'x', 'x'},
//		 {'x', ' ', 'x', ' ', ' ', ' ', 'x', 'x', 'x'},
//		 {'x', ' ', 'x', 'x', 'x', ' ', ' ', ' ', 'x'},
//		 {'x', ' ', ' ', ' ', 'x', 'x', 'x', ' ', 'x'},
//		 {'x', 'x', 'x', ' ', ' ', ' ', 'x', ' ', 'x'},
//		 {'x', 'x', 'x', 'x', 'x', ' ', ' ', ' ', 'x'},
//		 {'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x'},
//		};
	
	private char[] m_agent = 
	{ '^', '>', 'v', '<' };

	/**
	 * Process a primitive schema and return its enaction status.
	 * @param s The string code that represents the primitive schema to enact.
	 * @return The boolean feedback resulting from the schema enaction.
	 */

	public IEffect enact(String s) 
	{
		IEffect effect = null;
		
		if (s.equals(">"))
			effect = move();
		else if (s.equals("^"))
			effect = left();
		else if (s.equals("v"))
			effect = right();
		else if (s.equals("-"))
			effect = Touch();
		else if (s.equals("\\"))
			effect = TouchRight();
		else if (s.equals("/"))
			effect = TouchLeft();
		
		// print the maze
		for (int i = 0; i < HEIGHT; i++)
		{
			for (int j = 0; j < WIDTH; j++)
			{
				if (i == m_y && j== m_x)
					System.out.print(m_agent[m_o]);
				else
					System.out.print(m_board[i][j]);	
			}
			System.out.println();
		}
		
		return effect;
	}

	/**
	 * Turn to the right. 
	 */
	private IEffect right()
	{
		IEffect effect = new Effect();
		effect.setLabel("f");
		effect.setColor(0xFFFFFF);
		
		m_o++;
		
		if (m_o > ORIENTATION_LEFT)
			m_o = ORIENTATION_UP;

		// In the Simple Maze, the effect may vary according to the wall in front after turning
//		if (((m_o == ORIENTATION_UP) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ')) ||
//			((m_o == ORIENTATION_DOWN) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ')) ||
//			((m_o == ORIENTATION_RIGHT) && (m_x < WIDTH) && (m_board[m_y][m_x + 1] == ' ')) ||
//			((m_o == ORIENTATION_LEFT) && (m_x > 0) && (m_board[m_y][m_x - 1] == ' ')))
//			{effect.setLabel("t");effect.setColor(0x00FF00);}

		return effect;
	}
	
	/**
	 * Turn to the left. 
	 */
	private IEffect left()
	{
		IEffect effect = new Effect();
		effect.setLabel("f");
		effect.setColor(0xFFFFFF);
		
		m_o--;
		
		if (m_o < 0)
			m_o = ORIENTATION_LEFT;

		// In the Simple Maze, the effect may vary according to the wall in front after turning
//		if (((m_o == ORIENTATION_UP) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ')) ||
//			((m_o == ORIENTATION_DOWN) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ')) ||
//			((m_o == ORIENTATION_RIGHT) && (m_x < WIDTH) && (m_board[m_y][m_x + 1] == ' ')) ||
//			((m_o == ORIENTATION_LEFT) && (m_x > 0) && (m_board[m_y][m_x - 1] == ' ')))
//			{effect.setLabel("t");effect.setColor(0x00FF00);}

		return effect;
	}
	
	/**
	 * Move forward to the direction of the current orientation.
	 */
	private IEffect move()
	{
		IEffect effect = new Effect();
		effect.setLabel("f");
		effect.setColor(0xFF0000);

		boolean status = false;

		if ((m_o == ORIENTATION_UP) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ' ))
				{m_y--; effect.setLabel("t"); effect.setColor(0xFFFFFF);}

		if ((m_o == ORIENTATION_DOWN) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ' ))
				{m_y++; effect.setLabel("t"); effect.setColor(0xFFFFFF);}

		if ((m_o == ORIENTATION_RIGHT) && ( m_x < WIDTH ) && (m_board[m_y][m_x + 1] == ' ' ))
				{m_x++; effect.setLabel("t"); effect.setColor(0xFFFFFF);}

		if ((m_o == ORIENTATION_LEFT) && ( m_x > 0 ) && (m_board[m_y][m_x - 1] == ' ' ))
				{m_x--; effect.setLabel("t"); effect.setColor(0xFFFFFF);}

		if (!status)
			System.out.println("Ouch");

		return effect;
	}
	
	/**
	 * Touch the square forward.
	 * Succeeds if there is a wall, fails otherwise 
	 */
	private IEffect Touch()
	{
		IEffect effect = new Effect();
		effect.setLabel("t");
		effect.setColor(0x008000);

		if (((m_o == ORIENTATION_UP) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ')) ||
			((m_o == ORIENTATION_DOWN) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ')) ||
			((m_o == ORIENTATION_RIGHT) && (m_x < WIDTH) && (m_board[m_y][m_x + 1] == ' ')) ||
			((m_o == ORIENTATION_LEFT) && (m_x > 0) && (m_board[m_y][m_x - 1] == ' ')))
		   	{effect.setLabel("f");effect.setColor(0xFFFFFF);}

		return effect;
	}
	
	/**
	 * Touch the square to the right.
	 * Succeeds if there is a wall, fails otherwise. 
	 */
	private IEffect TouchRight()
	{
		IEffect effect = new Effect();
		effect.setLabel("t");
		effect.setColor(0x008000);

		if (((m_o == ORIENTATION_UP) && (m_x > 0) && (m_board[m_y][m_x + 1] == ' ')) ||
			((m_o == ORIENTATION_DOWN) && (m_x < WIDTH) && (m_board[m_y][m_x - 1] == ' ')) ||
			((m_o == ORIENTATION_RIGHT) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ')) ||
			((m_o == ORIENTATION_LEFT) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ')))
			{effect.setLabel("f");effect.setColor(0xFFFFFF);}

		return effect;
	}

	/**
	 * Touch the square forward.
	 * Succeeds if there is a wall, fails otherwise 
	 */
	private IEffect TouchLeft()
	{
		IEffect effect = new Effect();
		effect.setLabel("t");
		effect.setColor(0x008000);
	
		if (((m_o == ORIENTATION_UP) && (m_x > 0) && (m_board[m_y][m_x - 1] == ' ')) ||
			((m_o == ORIENTATION_DOWN) && (m_x < WIDTH) && (m_board[m_y][m_x + 1] == ' ')) ||
			((m_o == ORIENTATION_RIGHT) && (m_y > 0) && (m_board[m_y - 1][m_x] == ' ')) ||
			((m_o == ORIENTATION_LEFT) && (m_y < HEIGHT) && (m_board[m_y + 1][m_x] == ' ')))
			{effect.setLabel("f");effect.setColor(0xFFFFFF);}

		return effect;
	}

	public void initErnest(IErnest ernest) {
		ernest.addInteraction("-", "t", -2); // Touch wall
		ernest.addInteraction("-", "f", -1); // Touch empty
		ernest.addInteraction("\\", "t", -2);// Touch right wall
		ernest.addInteraction("\\", "f", -1);// Touch right empty
		ernest.addInteraction("/", "t", -2); // Touch left wall
		ernest.addInteraction("/", "f", -1); // Touch left empty
		ernest.addInteraction(">", "t",  5); // Move
		ernest.addInteraction(">", "f", -10);// Bump		
		ernest.addInteraction("v", "t", -3); // Right toward empty
		ernest.addInteraction("v", "f", -3); // Right toward wall		
		ernest.addInteraction("^", "t", -3); // Left toward empty
		ernest.addInteraction("^", "f", -3); // Left toward wall		
	}

	public void trace(ITracer tracer) {
		// TODO Auto-generated method stub
		
	}
}
