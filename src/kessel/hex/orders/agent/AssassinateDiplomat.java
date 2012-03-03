package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.GameItem;

/** Have an agent assassinate a diplomat. */
public class AssassinateDiplomat extends AbstractAssassinateFigure<Diplomat>
{
  public AssassinateDiplomat() { super(); } // GSON only

  public AssassinateDiplomat( Agent agent, Diplomat target, GameItem targetBase )
  {
    super( agent, target, targetBase );
  }
}
