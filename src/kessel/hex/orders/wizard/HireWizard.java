package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractHireFigure;

/** Recruit a new wizard. */
public class HireWizard extends AbstractHireFigure
{
  public HireWizard() {} // GSON only

  public HireWizard( King purchaser, PopCenter base )
  {
    super( purchaser, base );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleMaxedOnWizards( game ) ) return false;
    return true;
  }

  private boolean handleMaxedOnWizards( Game game )
  {
    if ( _subject.getOwner().getWizards().size() >= _subject.getOwner().getKingdom().getStartingWizards().size() )
    {
      addPlayerEvent( game, _subject, "The kingdom cannot support another wizard." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    String figureName = _subject.getOwner().nextFigureName();
    Wizard wizard = new Wizard( game.generateUniqueId(), figureName, 0, _base, _subject.getOwner() );
    wizard.setLevel( 0 );
    _subject.getOwner().add( wizard );
  }

  public int getOrderCost()
  {
    return 6 * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY;
  }

  public String getShortDescription()
  {
    return _base.getName() + _base.getLocation().getCoord() + " hire wizard";
  }
}
