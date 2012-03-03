package kessel.hex.orders.wizard;

import kessel.hex.domain.Wizard;

public class InvisibleArmy extends AbstractInvisibleArmy
{
  public InvisibleArmy() { super(); } // GSON only
  public InvisibleArmy( Wizard wizard )
  {
    super( wizard );
  }

  protected int determineInvisibleCapacity() { return _subject.getLevel(); }
}
