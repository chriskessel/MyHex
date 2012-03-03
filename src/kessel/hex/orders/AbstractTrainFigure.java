package kessel.hex.orders;

import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;

/** Train a figure's (improve it's level). */
public abstract class AbstractTrainFigure<T extends Figure> extends Order<T>
{
  protected AbstractTrainFigure() { super(); } // GSON only

  protected AbstractTrainFigure( T figure )
  {
    super( figure );
  }

  public void processOrder( Game game )
  {
    _subject.incrementLevel();
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleNotAtPopCenter( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleHitLevelCap( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleNotAtPopCenter( Game game )
  {
    PopCenter pop = game.getPopCenter( _subject.getLocation() );
    boolean isAtOwnedPop = pop != null && pop.getOwner().equals( _subject.getOwner() );
    if ( !isAtOwnedPop )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " must be at one of our kingdom's population center to train." );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, "Can not move " + _subject.getName() + " as there isn't enough gold in the treasury." );
      return true;
    }
    return false;
  }

  private boolean handleHitLevelCap( Game game )
  {
    if ( _subject.getLevel() >= _subject.getLevelCap() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " realized that further training would provide no improvement." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " train";
  }
}
