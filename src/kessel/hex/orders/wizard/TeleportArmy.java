package kessel.hex.orders.wizard;

import kessel.hex.domain.Wizard;
import kessel.hex.util.Tuple;

/** Teleport the wizard's group anywhere on the map. */
public class TeleportArmy extends AbstractTeleportArmy
{
  public TeleportArmy() { super(); } // GSON only
  public TeleportArmy( Wizard wizard, Tuple destination )
  {
    super( wizard, destination );
  }

  protected int determineTeleportCapacity() { return _subject.getLevel(); }
}
