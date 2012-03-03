package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;

public abstract class AbstractRaiseUnit extends AbstractSpell
{
  protected AbstractRaiseUnit() { super(); } // GSON only
  protected AbstractRaiseUnit( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotInArmy( game ) ) return false;
    return true;
  }

  boolean handleNotInArmy( Game game )
  {
    if ( _subject.getBase() instanceof PopCenter )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " must be in an army to summon." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    ((Army) _subject.getBase()).addUnit( new ArmyUnit( game.generateUniqueId(), getTroopType(), game.getTurn() ) );
  }

  protected abstract TroopType getTroopType();
}