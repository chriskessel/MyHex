package kessel.hex.orders.wizard;

import kessel.hex.domain.Wizard;
import kessel.hex.util.Tuple;

/** Teleport the wizard's group anywhere on the map. */
public class UnlimitedTeleportArmy extends AbstractTeleportArmy
{
  public UnlimitedTeleportArmy() { super(); } // GSON only
  public UnlimitedTeleportArmy( Wizard wizard, Tuple destination )
  {
    super( wizard, destination );
  }

  protected int determineTeleportCapacity() { return Integer.MAX_VALUE; }
}
