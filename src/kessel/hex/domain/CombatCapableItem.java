package kessel.hex.domain;

import kessel.hex.map.Location;

/** For those game items capable of combat */
public abstract class CombatCapableItem extends GameItem implements CombatCapable
{
  protected CombatCapableItem() { super(); } // GSON requires this.

  protected CombatCapableItem( int id, String name, int turnSeen, Location location )
  {
    super( id, name, turnSeen, location );
  }

  protected CombatCapableItem( int id, String name, int turnSeen, Location location, Player owner )
  {
    super( id, name, turnSeen, location, owner );
  }

  public int getCombatStrength() { return getCombatStrength( _location.getTerrain() ); }
}
