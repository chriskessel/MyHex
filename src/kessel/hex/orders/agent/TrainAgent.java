package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.orders.AbstractTrainFigure;

/** Train an agent. */
public class TrainAgent extends AbstractTrainFigure<Agent>
{
  public TrainAgent() { super(); } // GSON only.

  public TrainAgent( Agent agent )
  {
    super( agent );
  }

  public int getOrderCost()
  {
    return (int) (Game.BASE_HAMLET_PRODUCTION * 1.5 * Game.GOLD_GRANULARITY * _subject.getTrainingCostModifier());
  }
}
