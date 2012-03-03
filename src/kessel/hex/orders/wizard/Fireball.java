package kessel.hex.orders.wizard;

import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;

/** Fireball adds to it's army's combat strength. Damage is exponential with the wizard level: damage =  1/6V * getTotalLevels(). */
public class Fireball extends CombatSpell
{
  static final int BASE_DAMAGE = new ArmyUnit( TroopType.FIREBALL ).getBaseCombatStrength();

  public Fireball() { super(); } // GSON only
  public Fireball( Wizard wizard, GameItem target )
  {
    super( wizard, target );
  }

  public void processOrder( Game game )
  {
    // Nothing to do. It's up to the affected orders to check for the fireball.
    addPlayerEvent( game, _subject, _subject.getName() + " cast fireball against " + _target.getName() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " cast fireball against " + _target.getName();
  }

  /** @return the combat strength of the fireball. */
  public int getValue()
  {
    return BASE_DAMAGE * _subject.getTotalLevels();
  }
}
