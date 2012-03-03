package kessel.hex.domain;

import kessel.hex.map.Terrain;

/** Defines abilities for items that can fight. */
public interface CombatCapable
{
  /**
   * @return the unit's combat strength, ignoring external factors like terrain and regional control. Internal factors, like morale and
   *         wounded do apply.
   */
  int getBaseCombatStrength();

  /** @return the unit's combat strength in the given terrain. */
  int getCombatStrength( Terrain terrain );

  /** @param hits The amount of damage to take. Actual casualties depend on the troop type and how it takes wounds. */
  void takeDamage( int hits );
}
