package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.util.Tuple;

import java.util.List;

/** Move an army. */
public class ArmyMove extends AbstractArmyMove
{
  public ArmyMove() { super(); } // for GSON only
  public ArmyMove( Army army, List<Tuple> movementSteps )
  {
    super( army, movementSteps );
  }

  protected boolean shouldStop( Tuple hex, Game game )
  {
    return false; // Regular movement doesn't stop for any reason.
  }
}
