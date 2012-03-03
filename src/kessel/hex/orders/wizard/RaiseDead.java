package kessel.hex.orders.wizard;

import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;

public class RaiseDead extends AbstractRaiseUnit
{
  public RaiseDead() { super(); } // GSON only
  public RaiseDead( Wizard wizard )
  {
    super( wizard );
  }

  protected TroopType getTroopType()
  {
    return TroopType.UNDEAD;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " recruit undead";
  }
}
