package kessel.hex.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Treats a hex as if it's inside a square. For values inside the square, but outside the hex, the calculator can figure out to which
 * neighbor the point actually belongs.
 */
@SuppressWarnings({ "ConstantMathCall", "JavaDoc" })
public class HexCalculator
{
  private static final int[] NEIGHBORS_DI = { 0, 1, 1, 0, -1, -1 };
  private static final int[][] NEIGHBORS_DJ = { { -1, -1, 0, 1, 0, -1 }, { -1, 0, 1, 1, 1, 0 } };

  private final int[] xVertices; // array of horizontal offsets of the cell's corners
  private final int[] yVertices; // array of vertical offsets of the cell's corners
  private final int[] xInsets;   // array of horizontal offsets to get corners further in or out from the norm.
  private final int[] yInsets;  // array of vertical offsets to get corners further in or out from the norm.

  // All values in pixels.
  public final int _radius;
  public final int _height;
  public final int _width;
  public final int _side;

  /** @param radius Cell radius (distance from the center to one of the corners) */
  public HexCalculator( int radius )
  {
    _radius = radius;
    _width = radius * 2;
    _height = (int) (((float) radius) * Math.sqrt( 3 ));
    _side = radius * 3 / 2;
    xVertices = new int[] { _radius / 2, _side, _width, _side, _radius / 2, 0 };
    yVertices = new int[] { 0, 0, _height / 2, _height, _height, _height / 2 };
    xInsets = new int[] { 0, 0, -1, 0, 0, 1 };
    yInsets = new int[] { 1, 1, 0, -1, -1, 0 };
  }

  /**
   * Computes X and Y grid coordinates for all of the cell's 6 corners, clockwise, starting from the top left. Based on a particular hex
   * at the given location.
   *
   * @param gridX the map X grid coordinate
   * @param gridY the map Y grid coordinate
   * @param inset how many pixels inside the corner rather than on it. Negative number push the corners out.
   * @return an 2 element array of arrays, with the 0th element being an array of X pixels, and the 1st being an array of Y pixels.
   */
  public int[][] computePixelCorners( int gridX, int gridY, int inset )
  {
    Tuple pixelGrids = getPixelByGrid( gridX, gridY );
    int[] cornersX = new int[6];
    int[] cornersY = new int[6];
    for ( int cornerIndex = 0; cornerIndex < 6; cornerIndex++ )
    {
      cornersX[cornerIndex] = pixelGrids.x + xVertices[cornerIndex] + (inset * xInsets[cornerIndex]);
      cornersY[cornerIndex] = pixelGrids.y + yVertices[cornerIndex] + (inset * yInsets[cornerIndex]);
    }
    return new int[][] { cornersX, cornersY };
  }

  /** Gets the cell's horizontal and vertical pixel coordinates. */
  public Tuple getPixelByGrid( int gridX, int gridY )
  {
    int pixelX = gridX * _side;
    int pixelY = _height * (2 * gridY + (gridX % 2)) / 2;
    return new Tuple( pixelX, pixelY );
  }

  /** The center of a grid cell. */
  public Tuple getPixelCenterByGrid( Tuple coord )
  {
    return getPixelCenterByGrid( coord.x, coord.y );
  }

  public Tuple getPixelCenterByGrid( int gridX, int gridY )
  {
    // The average of the highest and lowest corner should be the middle.
    int[][] corners = computePixelCorners( gridX, gridY, 0 );
    int highestX = corners[0][0];
    int lowestX = corners[0][0];
    for ( int x : corners[0] )
    {
      highestX = Math.max( x, highestX );
      lowestX = Math.min( x, lowestX );
    }
    int highestY = corners[1][0];
    int lowestY = corners[1][0];
    for ( int y : corners[1] )
    {
      highestY = Math.max( y, highestY );
      lowestY = Math.min( y, lowestY );
    }
    return new Tuple( (highestX + lowestX) / 2, (highestY + lowestY) / 2 );
  }

  /** Get the actual grid coordinates from pixel coordinates. */
  public Tuple getGridByPixel( Tuple pixelCoord )
  { return getGridByPixel( pixelCoord.x, pixelCoord.y ); }

  public Tuple getGridByPixel( int inPixelX, int inPixelY )
  {
    int gridX = (int) Math.floor( (float) inPixelX / (float) _side );
    int pixelX = inPixelX - _side * gridX;

    int ty = inPixelY - (gridX % 2) * _height / 2;
    int gridY = (int) Math.floor( (float) ty / (float) _height );
    int cy = ty - _height * gridY;

    if ( pixelX > Math.abs( _radius / 2 - _radius * cy / _height ) )
    {
      return new Tuple( gridX, gridY );
    }
    else
    {
      return new Tuple( gridX - 1, gridY + (gridX % 2) - ((cy < _height / 2) ? 1 : 0) );
    }
  }


  // Static Helpers. These don't need pixel information.
  // ---------------------------------------------------
  public static Tuple getGridNeighbor( Tuple tuple, int direction )
  {
    return new Tuple(
      getGridXNeighbor( tuple.x, tuple.y, direction ),
      getGridYNeighbor( tuple.x, tuple.y, direction ) );
  }

  /** @return Horizontal grid coordinate for the given neighbor. */
  public static int getGridXNeighbor( int gridX, int gridY, int neighborIdx )
  {
    return gridX + NEIGHBORS_DI[neighborIdx];
  }

  /** @return Vertical grid coordinate for the given neighbor. */
  public static int getGridYNeighbor( int gridX, int gridY, int neighborIdx )
  {
    return gridY + NEIGHBORS_DJ[Math.abs( gridX % 2 )][neighborIdx];
  }

  public static List<Tuple> getAllNeighbors( Tuple startingTuple, int hexRadius )
  {
    return getNeighbors( startingTuple, hexRadius, false );
  }

  public static List<Tuple> getEdgeNeighbors( Tuple startingTuple, int hexRadius )
  {
    return getNeighbors( startingTuple, hexRadius, true );
  }

  /** @return the target hex closes to the source or null if it can't be found. Randomly picks between equidistant targets. */
  public static Tuple getClosest( Tuple source, List<Tuple> targets, int maxRadius )
  {
    for ( int i = 0; i <= maxRadius; i++ )
    {
      List<Tuple> candidates = getEdgeNeighbors( source, i );
      candidates.retainAll( targets );
      if ( !candidates.isEmpty() )
      {
        return candidates.get( 0 );
      }
    }
    return null;
  }

  /** Get all the neighbors within the radius given. */
  public static List<Tuple> getNeighbors( Tuple center, int radius, boolean edgeOnly )
  {
    Set<Tuple> stuff = new HashSet<>();
    if ( !edgeOnly || (radius == 0) )
    {
      stuff.add( center );
    }
    for ( int direction = 0; direction < 6; direction++ )
    {
      Tuple currentHex = center;
      for ( int stepsTaken = 1; stepsTaken <= radius; stepsTaken++ )
      {
        // Step in the direction, add the hex.
        Tuple mainStep = getGridNeighbor( currentHex, direction );
        currentHex = mainStep;
        if ( !edgeOnly || (stepsTaken == radius) )
        {
          stuff.add( currentHex );
        }

        // Turn right 60 degrees, add hexes for the remaining steps.
        int stepsToTheRight = radius - (stepsTaken);
        for ( int i = 1; i <= stepsToTheRight; i++ )
        {
          currentHex = getGridNeighbor( currentHex, (direction + 1) % 6 );
          if ( !edgeOnly || (stepsTaken + i == radius) )
          {
            stuff.add( currentHex );
          }
        }

        // We're done going to the right, reset for the next step in the original direction.
        currentHex = mainStep;
      }
    }
    return new ArrayList<>( stuff );
  }

  public static int floor2( int i ) { return i >= 0 ? i >> 1 : (i - 1) / 2; }

  public static int ceil2( int i ) { return i >= 0 ? (i + 1) >> 1 : i / 2; }

  public static int calculateDistance( Tuple a, Tuple b )
  {
    int sane_y1 = a.y - a.x / 2;
    int sane_y2 = b.y - b.x / 2;

    int dx = b.x - a.x;
    int dy = sane_y2 - sane_y1;

    int dist = dx * dy > 0 ? Math.abs( dx + dy ) : Math.max( Math.abs( dx ), Math.abs( dy ) );
    return dist;
//        // Swap the square hex grid coords for ones rotated by 60 degrees clockwise.
//        Tuple newA = new Tuple( a.x - ( a.y / 2), a.y );
//        Tuple newB = new Tuple( b.x - ( b.y / 2 ), b.y );
//
//        // Now use the following calculating, which Google searches indicate is pretty standard hex coordinate math.
//        return rotatedDistance( newA, newB );
  }

  private static int rotatedDistance( Tuple a, Tuple b )
  {
    if ( a.x > b.x )
    {
      return rotatedDistance( b, a );
    }
    else if ( b.y >= a.y )
    {
      return (b.x - a.x) + (b.y - a.y);
    }
    else
    {
      return Math.max( b.x - a.x, a.y - b.y );
    }
  }

  /** @return true if the hexes are adjacent. */
  public static boolean isAdjacent( Tuple lastHex, Tuple nextHex )
  {
    return getNeighbors( lastHex, 1, true ).contains( nextHex );
  }
}