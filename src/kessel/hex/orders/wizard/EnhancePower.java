package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

/** Enhanced the power of the wizard's king. */
public class EnhancePower extends AbstractSpell
{
  public EnhancePower() { super(); } // GSON only
  public EnhancePower( Wizard wizard )
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

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _subject.getOwner().getKing() ) )
    {
      addPlayerEvent( game, _subject,
        _subject.getName() + "'s attempt to enhance the king's power was blocked by magical protection." );
      return true;
    }
    return false;
  }

  protected void updateViews( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " increased the king's power." );
  }

  public void processOrder( Game game )
  {
    _subject.getOwner().adjustPower( 1 );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " enhance the king's power.";
  }
}
