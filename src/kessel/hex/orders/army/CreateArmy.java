package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.Player;
import kessel.hex.orders.Order;
import kessel.hex.orders.PolicyOrder;

/** Create a new army. It starts as inactive at the capitol. */
public class CreateArmy extends Order<King> implements PolicyOrder
{
  public CreateArmy() { super(); } // GSON only
  public CreateArmy( King purchaser )
  {
    super( purchaser );
  }

  @SuppressWarnings({ "SimplifiableIfStatement", "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleNotEnoughMoney( game ) ) return false;
    if ( handleNotEnoughSupport( game ) ) return false;
    return true;
  }

  protected boolean handleNotEnoughMoney( Game game )
  {
    if ( _subject.getOwner().getGold() < getOrderCost() )
    {
      addPlayerEvent( game, _subject, "Can not create an army as there is not enough gold in the treasury." );
      return true;
    }
    return false;
  }

  protected boolean handleNotEnoughSupport( Game game )
  {
    if ( _subject.getOwner().getSupportCapacity() < _subject.getOwner().getSupportRequired() )
    {
      addPlayerEvent( game, _subject, "The kingdom cannot support any more armies." );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    Player player = _subject.getOwner();
    Army army = new Army(
      game.generateUniqueId(), _subject.getOwner().nextArmyName(),
      game.getTurn(), player.getCapitol().getLocation() );
    player.add( army );
  }

  public int getOrderCost()
  {
    return 2 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public String getShortDescription()
  {
    return "Create the " + _subject.getOwner().nextArmyName();
  }
}
