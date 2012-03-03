package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

/** Increase the level of a population center at the wizard's location. */
public class ImbuePopCenter extends AbstractSpell
{
  public ImbuePopCenter() { super(); } // GSON only

  public ImbuePopCenter( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "IfStatementWithIdenticalBranches" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    else if ( handleNoPop( game ) ) return false;
    return true;
  }

  protected boolean handleNoPop( Game game )
  {
    if ( game.getPopCenter( _subject.getLocation().getCoord() ) == null )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " found no pop center to imbue with power." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    PopCenter target = game.getPopCenter( _subject.getLocation().getCoord() );
    target.improveLevel();
  }

  public String getShortDescription()
  {
    return _subject.getName() + " imbue pop center";
  }

  protected void updateViews( Game game )
  {
    PopCenter target = game.getPopCenter( _subject.getLocation().getCoord() );
    addPlayerEvent( game, _subject, _subject.getName() + " imbued " + target.getName() + " with power and improved it's level." );
  }
}
