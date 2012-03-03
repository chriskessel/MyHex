package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractTrainFigure;

/** Train a wizard. */
public class TrainWizard extends AbstractTrainFigure<Wizard>
{
  public TrainWizard() { super(); }

  public TrainWizard( Wizard wizard )
  {
    super( wizard );
  }

  public int getOrderCost()
  {
    int level = _wasExecuted ? _subject.getLevel() : _subject.getLevel() + 1;
    return (int) (level * Game.BASE_HAMLET_PRODUCTION * 3.0 * Game.GOLD_GRANULARITY *
                  _subject.getTrainingCostModifier());
  }
}
