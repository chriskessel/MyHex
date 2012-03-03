package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

/** Improve every population center in the region by 1. */
public class ImbueRegion extends AbstractSpell
{
  public ImbueRegion() { super(); } // GSON only
  public ImbueRegion( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    return true;
  }

  protected void processOrder( Game game )
  {
    for ( PopCenter popCenter : _subject.getLocation().getRegion().getPopCenters() )
    {
      popCenter.improveLevel();
    }
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not imbue " + _subject.getLocation().getRegion().getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " imbue " + _subject.getLocation().getRegion().getName();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " imbued the region of " + _subject.getLocation().getRegion().getName() );
  }
}
