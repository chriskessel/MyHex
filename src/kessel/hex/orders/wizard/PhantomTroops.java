package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Wizard;

/**
 * Create illusory troops to help absorb damage in combat. They provide no combat value though. The spell has no value except against the
 * target army/pop and only if there is a combat with that army/pop. Damage can be reduced to zero.
 */
public class PhantomTroops extends CombatSpell
{
  public PhantomTroops() { super(); } // GSON only
  public PhantomTroops( Wizard wizard, GameItem target )
  {
    super( wizard, target );
  }

  public void processOrder( Game game )
  {
    // Nothing to do. It's up to the affected orders to check for the existence of phantom troops.
    addPlayerEvent( game, _subject, _subject.getName() + " created phantom troops against " + _target.getName() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " create phantom troops against " + _target.getName();
  }

  /** @return the percentage of damage reduction (e.g. 20 = 20% reduction). */
  public int getValue() { return 10 * _subject.getLevel(); }
}
