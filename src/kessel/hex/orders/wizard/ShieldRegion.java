package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

public class ShieldRegion extends AbstractSpell
{
  public ShieldRegion() { super(); } // GSON only
  public ShieldRegion( Wizard wizard )
  {
    super( wizard );
  }

  protected void processOrder( Game game )
  {
    // Nothing, just the existence of the order matters. Orders affected must make the check for regional shielding.
  }

  public String getShortDescription()
  {
    return _subject.getName() + " protects the region of " + _subject.getLocation().getRegion().getName();
  }
}
