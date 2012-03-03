package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Hide a wizard's group from recons, intel reports, etc. */
public abstract class AbstractInvisibleArmy extends AbstractSpell
{
  protected AbstractInvisibleArmy() { super(); } // GSON only
  protected AbstractInvisibleArmy( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotInArmy( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleArmyTooBig( game ) ) return false;
    return true;
  }

  private boolean handleNotInArmy( Game game )
  {
    if ( !(_subject.getBase() instanceof Army) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not based in an army and thus could not cast the invisibility." );
      return true;
    }
    return false;
  }

  private boolean handleArmyTooBig( Game game )
  {
    if ( ((Army) _subject.getBase()).getUnits().size() > determineInvisibleCapacity() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " wasn't able to hide such a large army." );
      return true;
    }
    return false;
  }

  protected abstract int determineInvisibleCapacity();

  protected void processOrder( Game game )
  {
    _subject.getBase().setInvisible( true );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " hide " + _subject.getBase().getName();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " made the " + _subject.getBase().getName() + " invisible." );
  }
}
