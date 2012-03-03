package kessel.hex.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** An arbitrary tuple */
public class Tuple
{
  public final int x, y;

  public Tuple( int x, int y )
  {
    this.x = x;
    this.y = y;
  }

  public boolean equals( Object that )
  {
    return EqualsBuilder.reflectionEquals( this, that );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this );
  }

  public String toString()
  {
    return "(" + x + "," + y + ")";
  }
}
