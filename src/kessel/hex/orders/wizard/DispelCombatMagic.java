package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Wizard;

/**
 * Attempt to dispel combat magic cast by any wizard in the target army. One DispelCombatMagic spell can dispel multiple opposing combat
 * spells. This spell can be cast by a wizard in a defending population center or against a defending population center.
 */
public class DispelCombatMagic extends CombatSpell
{
  public DispelCombatMagic() { super(); } // GSON only
  public DispelCombatMagic( Wizard wizard, GameItem target )
  {
    super( wizard, target );
  }

  public void processOrder( Game game )
  {
    // Nothing to do. It's up to the affected orders to check for the existence of dispel magic.
    addPlayerEvent( game, _subject, _subject.getName() + " attempted to dispel any combat magic cast by " + _target.getName() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " attempt to dispel any combat magic cast by " + _target.getName();
  }

  public int getValue() { return 0; } // Not relevant.
}
