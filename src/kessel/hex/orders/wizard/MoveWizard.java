package kessel.hex.orders.wizard;

import kessel.hex.domain.GameItem;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractMoveFigure;

/** Move a wizard's base of operations. */
public class MoveWizard extends AbstractMoveFigure<Wizard>
{
  public MoveWizard() {} // GSON only

  public MoveWizard( Wizard agent, GameItem newBase )
  {
    super( agent, newBase );
  }

  public int getOrderCost()
  {
    // Wizards move for free.
    return 0;
  }
}
