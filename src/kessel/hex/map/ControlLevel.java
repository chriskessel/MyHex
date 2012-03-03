package kessel.hex.map;

/** How much a player controls of a region. */
public enum ControlLevel
{
  Unknown( -1 ), None( 0 ), Presence( 1 ), Control( 51 ), Domination( 75 );
  private final int _percentRequired;

  ControlLevel( int percentRequired ) { this._percentRequired = percentRequired; }

  public int getPercentRequired() { return _percentRequired; }
}
