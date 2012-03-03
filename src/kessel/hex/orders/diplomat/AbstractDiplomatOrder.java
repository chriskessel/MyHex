package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.orders.Order;

/** Common functionality for all diplomat orders. */
public abstract class AbstractDiplomatOrder extends Order<Diplomat>
{
  protected AbstractDiplomatOrder() { super(); } // GSON only
  protected AbstractDiplomatOrder( Diplomat subject )
  {
    super( subject );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient treasury to execute the diplomatic action." );
      return true;
    }
    return false;
  }

  public int getOrderCost()
  {
    return (_subject.getLevel() * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY) / 5;
  }
}
