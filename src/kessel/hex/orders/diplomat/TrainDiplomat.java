package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.orders.AbstractTrainFigure;

/** Train a diplomat. */
public class TrainDiplomat extends AbstractTrainFigure<Diplomat>
{
  public TrainDiplomat() { super(); }

  public TrainDiplomat( Diplomat diplomat )
  {
    super( diplomat );
  }

  public int getOrderCost()
  {
    return (int) (Game.BASE_HAMLET_PRODUCTION * 2.0 * Game.GOLD_GRANULARITY * _subject.getTrainingCostModifier());
  }
}
