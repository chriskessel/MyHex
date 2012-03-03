package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.GameItem;

/** Have an agent assassinate an agent. */
public class AssassinateAgent extends AbstractAssassinateFigure<Agent>
{
  public AssassinateAgent() { super(); } // GSON only

  public AssassinateAgent( Agent agent, Agent target, GameItem targetBase )
  {
    super( agent, target, targetBase );
  }
}
