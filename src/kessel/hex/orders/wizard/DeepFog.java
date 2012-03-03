package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Creates a dense fog that prevents combat with the designated target. An army casting deep fog */
public class DeepFog extends CombatSpell
{
  public DeepFog() { super(); } // GSON only
  public DeepFog( Wizard wizard, Army target )
  {
    super( wizard, target );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleWizardNotInArmy( game ) ) return false;
    return true;
  }

  private boolean handleWizardNotInArmy( Game game )
  {
    if ( !(_subject.getBase() instanceof Army) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " can only cast deep fog if based in an army." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    // Nothing to do. It's up to the affected orders to check for the existence of deep fog.
    addPlayerEvent( game, _subject, _subject.getName() + " created a deep fog around " + _target.getName() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " create a deep fog around " + _target.getName();
  }

  public int getValue() { return 0; } // Not relevant since fog prevents combat.
}
