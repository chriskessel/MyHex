package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.util.Tuple;

import java.util.List;

/** Move an army, but stop if it finds a population center. */
public class ArmySearch extends AbstractArmyMove
{
  public ArmySearch() { super(); } // for GSON only
  public ArmySearch( Army army, List<Tuple> movementSteps )
  {
    super( army, movementSteps );
  }

  protected boolean shouldStop( Tuple hex, Game game )
  {
    // Stop if we find a pop center at the hex.
    return game.getPopCenter( hex ) != null;
  }
}
