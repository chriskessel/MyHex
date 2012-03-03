package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.orders.Order;

/** Common items for all Army orders. */
public abstract class AbstractArmyOrder extends Order<Army>
{
  protected AbstractArmyOrder() { super(); } // for GSON only

  protected AbstractArmyOrder( Army subject ) {super( subject );}

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleSubjectInactive( game ) ) return false;
    return true;
  }

  protected boolean handleSubjectInactive( Game game )
  {
    if ( !_subject.isActive() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not an active army." );
      return true;
    }
    return false;
  }
}
