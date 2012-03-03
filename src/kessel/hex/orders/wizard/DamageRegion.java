package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

/** Damage every population center in the region by 1. */
public class DamageRegion extends AbstractSpell
{
  public DamageRegion() { super(); } // GSON only
  public DamageRegion( Wizard wizard )
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
      popCenter.degradeLevel();
    }
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not corrupt " + _subject.getLocation().getRegion().getName() +
                                      " because the region was magically shielded." );
      return true;
    }
    return false;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " corrupt " + _subject.getLocation().getRegion().getName();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " corrupted the region of " + _subject.getLocation().getRegion().getName() );
  }
}
