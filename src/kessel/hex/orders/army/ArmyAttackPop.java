package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.PopCenter;
import org.apache.log4j.Logger;

/** Have one army attack a pop center. */
public class ArmyAttackPop extends AbstractArmyAttack
{
  private static final Logger LOG = Logger.getLogger( ArmyAttackPop.class );

  public ArmyAttackPop() { super(); } // GSON only
  public ArmyAttackPop( Army army, PopCenter target )
  {
    super( army, target );
  }
}
