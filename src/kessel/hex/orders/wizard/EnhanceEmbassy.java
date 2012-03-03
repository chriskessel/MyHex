package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Enhance the player's embassy in the wizard's region. */
public class EnhanceEmbassy extends AbstractSpell
{
  public EnhanceEmbassy() { super(); } // GSON only
  public EnhanceEmbassy( Wizard wizard )
  {
    super( wizard );
  }

  protected void processOrder( Game game )
  {
    _subject.getOwner().improveEmbassy( _subject.getLocation().getRegion() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " magically augment the embassy in " + _subject.getLocation().getRegion().getName();
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " augmented our embassy in " + _subject.getLocation().getRegion().getName() );
  }
}
