package kessel.hex.domain;

import kessel.hex.map.Location;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** Events that happen in the game. The intent is for a human friendly event report. */
public class GameEvent
{
  private final String _description;
  private final Location _location;
  private final int _turn;

  public GameEvent( String what, Location where, int when )
  {
    _description = what;
    _location = where;
    _turn = when;
  }

  public String getDescription() { return _description; }

  public Location getLocation() { return _location; }

  public int getTurn() { return _turn; }

  public boolean equals( Object o )
  {
    return EqualsBuilder.reflectionEquals( this, o );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this );
  }

  public String toString()
  {
    return ToStringBuilder.reflectionToString( this );
  }
}
