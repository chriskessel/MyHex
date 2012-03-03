package kessel.hex.orders.army;

import kessel.hex.domain.Army;

/**
 * Combine two armies. This is just a shortcut for a ArmyTransfer order that transfers everything. You can combine an active army into an
 * inactive, which effectively renames it.
 */
public class ArmyCombine extends ArmyTransfer
{
  public ArmyCombine() { super(); } // GSON only
  public ArmyCombine( Army army, Army target )
  {
    super( army, target, army.getAllItems() );
  }
}
