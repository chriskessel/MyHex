package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Wizard;

/** Have an agent assassinate a wizard. */
public class AssassinateWizard extends AbstractAssassinateFigure<Wizard>
{
  public AssassinateWizard() { super(); } // GSON only

  public AssassinateWizard( Agent agent, Wizard target, GameItem targetBase )
  {
    super( agent, target, targetBase );
  }
}
