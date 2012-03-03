package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.map.ControlLevel;
import kessel.hex.orders.Order;

/** Have an army retire it's weakest unit. */
public class RetireUnit extends AbstractArmyOrder
{
  public RetireUnit() { super(); } // GSON only
  public RetireUnit( Army army )
  {
    super( army );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    return true;
  }

  public void processOrder( Game game )
  {
    _subject.retireUnit();
    if ( !_subject.isActive() )
    {
      _subject.setLocation( _subject.getOwner().getCapitol().getLocation() );
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " retire unit";
  }
}
