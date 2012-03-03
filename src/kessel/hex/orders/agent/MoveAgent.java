package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.GameItem;
import kessel.hex.orders.AbstractMoveFigure;

/** Move an Agent's base of operations. */
public class MoveAgent extends AbstractMoveFigure<Agent>
{
  public MoveAgent() { super(); } // GSON only

  public MoveAgent( Agent agent, GameItem newBase )
  {
    super( agent, newBase );
  }
}
