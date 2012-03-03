package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Grants the wizard's army a diplomatic bonus when attempting DemandSurrender. Multiple aura spells combine in effect. */
@SuppressWarnings({ "IfStatementWithIdenticalBranches" })
public class DiplomaticAura extends AbstractSpell
{
  public DiplomaticAura() { super(); } // GSON only
  public DiplomaticAura( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    else if ( handleNotInGroup( game ) ) return false;
    return true;
  }

  private boolean handleNotInGroup( Game game )
  {
    if ( !(_subject.getBase() instanceof Army) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not based in an army and does not cast Diplomatic Aura." );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    // Nothing, just the existence of the order matters. The DemandSurrender order will adjust to it.
  }

  public String getShortDescription()
  {
    return _subject.getName() + " enhance the charisma of the leaders of the " + _subject.getBase().getName();
  }
}
