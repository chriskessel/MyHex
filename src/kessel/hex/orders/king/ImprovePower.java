package kessel.hex.orders.king;

import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.orders.Order;
import kessel.hex.orders.PolicyOrder;

/** Raise a player's power level. */
public class ImprovePower extends Order<King> implements PolicyOrder
{
  public ImprovePower() {} // GSON only

  public ImprovePower( King king )
  {
    super( king );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleIsCharmed( game ) ) return false;
    if ( handleKingWasAlreadyBusy( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    return true;
  }

  private boolean handleKingWasAlreadyBusy( Game game )
  {
    if ( _subject.hasIssuedPolicyOrder() )
    {
      addPlayerEvent( game, _subject, "Unable to improve power since " + _subject.getName() +
                                      " has already issued a policy order this turn." );
      return true;
    }
    return false;
  }

  private boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " lacked sufficient funds to improve the king's power." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _subject.getOwner().adjustPower( +1 );
  }

  public int getOrderCost()
  {
    return Game.GOLD_GRANULARITY * Game.BASE_HAMLET_PRODUCTION * 5;
  }

  public String getShortDescription()
  {
    return "Improve Power";
  }
}
