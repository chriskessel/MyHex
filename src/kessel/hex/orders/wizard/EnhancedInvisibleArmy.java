package kessel.hex.orders.wizard;

import kessel.hex.domain.Wizard;

public class EnhancedInvisibleArmy extends AbstractInvisibleArmy
{
  public EnhancedInvisibleArmy() { super(); } // GSON only
  public EnhancedInvisibleArmy( Wizard wizard )
  {
    super( wizard );
  }

  protected int determineInvisibleCapacity() { return _subject.getLevel() * 2; }
}
