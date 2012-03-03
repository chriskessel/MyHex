package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractHireFigure;

/** Recruit a new diplomat. */
public class HireDiplomat extends AbstractHireFigure
{
  public HireDiplomat() {} // GSON only

  public HireDiplomat( King purchaser, PopCenter base )
  {
    super( purchaser, base );
  }

  public void processOrder( Game game )
  {
    String figureName = _subject.getOwner().nextFigureName();
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), figureName, 0, _base, _subject.getOwner() );
    diplomat.setLevel( 3 );
    _subject.getOwner().add( diplomat );
  }

  public int getOrderCost()
  {
    return 2 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public String getShortDescription()
  {
    return _base.getName() + _base.getLocation().getCoord() + " hire diplomat";
  }
}
