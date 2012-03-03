package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;

/**
 * The diplomat extorts the population center to be loyal to their current owner. If multiple such orders are executed in the same
 * PopCenter, only the strongest applies (i.e. they aren't cumulative). It is possible for a kingdom to foil their own attempts if they
 * execute this in a town in which they're also making a AbstractDiplomaticAttempt.
 */
public class DiplomatInspireLoyalty extends AbstractDiplomaticAttempt
{
  public DiplomatInspireLoyalty() { super(); } // GSON only
  public DiplomatInspireLoyalty( Diplomat subject ) { super( subject ); }

  protected boolean canExecute( Game game )
  {
    return super.canExecute( game );
  }

  protected boolean makeAttempt( Game game )
  {
    // The order always succeeds.
    return true;
  }

  protected void handleAttemptSuccess( Game game )
  {
    // Nothing required.
  }

  protected void handleAttemptFailure( Game game )
  {
    // Not relevant for this order.
  }

  protected void updateViews( Game game )
  {
    // No view changes due to this.
  }

  public String getShortDescription()
  {
    return _subject.getName() + " inspire loyalty in " + _subject.getBase().getName() + _subject.getLocation().getCoord();
  }

  protected int determinePopResistance( Game game )
  {
    // Not relevant for this order.
    return 0;
  }
}
