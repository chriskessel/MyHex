package kessel.hex.orders;

import java.util.Random;

/** A collection of utility methods for game mechanics. */
public class Mechanics
{
  private static final Random _r = new Random();

  /**
   * Make a standard level roll => level + roll 1D2/level with 50% success rate.
   *
   * @return the number of hits (or successes).
   */
  public static int standardLevelRoll( int level )
  {
    int hits = level;
    for ( int i = 0; i < level; i++ )
    {
      hits += _r.nextInt( 2 ) % 2;
    }
    return hits;
  }
}
