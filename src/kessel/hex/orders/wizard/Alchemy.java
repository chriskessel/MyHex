package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Wizard generates gold. */
public class Alchemy extends AbstractSpell
{
  public Alchemy() { super(); } // GSON only
  public Alchemy( Wizard wizard )
  {
    super( wizard );
  }

  protected void processOrder( Game game )
  {
    _subject.getOwner().adjustGold( determineGoldCreated() );
  }

  private int determineGoldCreated()
  {
    return (int) (_subject.getLevel() * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY * 0.5);
  }

  public String getShortDescription()
  {
    return _subject.getName() + " turns stone into gold!";
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " created " + determineGoldCreated() + " gold." );
  }
}
