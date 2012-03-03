package kessel.hex.orders.wizard;

import kessel.hex.domain.Wizard;

public class UnlimitedInvisibleArmy extends AbstractInvisibleArmy
{
  public UnlimitedInvisibleArmy() { super(); } // GSON only
  public UnlimitedInvisibleArmy( Wizard wizard )
  {
    super( wizard );
  }

  protected int determineInvisibleCapacity() { return Integer.MAX_VALUE; }
}
