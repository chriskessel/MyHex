package kessel.hex.map;

import kessel.hex.util.Tuple;

/** A very simple POJO for a location (hex on the board). */
public class Location
{
  // Represents an unknown location. Use this instead when you'd want to use null for a location.
  public static final Location NOWHERE = new Location( -1, -1 );

  private Tuple _coord;
  private Terrain _terrain = Terrain.Undefined;
  private transient Region _region = Region.UNKNOWN_REGION;

  Location() {} // JSON support only.

  public Location( int x, int y )
  {
    _coord = new Tuple( x, y );
  }

  public Tuple getCoord() { return _coord; }

  public Terrain getTerrain() { return _terrain; }

  public void setTerrain( Terrain terrain ) { _terrain = terrain; }

  public Region getRegion() { return _region; }

  public void setRegion( Region region ) { _region = region; }

  public boolean equals( Object o )
  {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    Location location = (Location) o;
    return _coord.equals( location._coord );
  }

  public int hashCode()
  {
    return _coord.hashCode();
  }
}
