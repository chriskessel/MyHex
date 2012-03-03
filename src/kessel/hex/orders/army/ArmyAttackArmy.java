package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import org.apache.log4j.Logger;

/** Have one army attack another. */
public class ArmyAttackArmy extends AbstractArmyAttack
{
  private static final Logger LOG = Logger.getLogger( ArmyAttackArmy.class );

  public ArmyAttackArmy() { super(); } // GSON only
  public ArmyAttackArmy( Army army, Army target )
  {
    super( army, target );
  }
}
