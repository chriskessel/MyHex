package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractHireFigure;

/** Recruit a new agent. */
public class HireAgent extends AbstractHireFigure
{
  public HireAgent() { super(); } // GSON only.

  public HireAgent( King purchaser, PopCenter base )
  {
    super( purchaser, base );
  }

  public void processOrder( Game game )
  {
    String figureName = _subject.getOwner().nextFigureName();
    Agent agent = new Agent( game.generateUniqueId(), figureName, 0, _base, _subject.getOwner() );
    agent.setLevel( 2 );
    _subject.getOwner().add( agent );
  }

  public int getOrderCost()
  {
    return 2 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public String getShortDescription()
  {
    return _base.getName() + _base.getLocation().getCoord() + " hire agent";
  }
}
