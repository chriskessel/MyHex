package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractMoveFigure;

import java.util.Random;

/** Move a Diplomat's base of operations. */
public class MoveDiplomat extends AbstractMoveFigure<Diplomat>
{
  /** How often the diplomat's action is reported to the affected player. */
  private final transient Random _r = new Random();
  public transient int _percentSeen = 50;

  public MoveDiplomat() { super(); } // GSON only

  public MoveDiplomat( Diplomat diplomat, GameItem newBase )
  {
    super( diplomat, newBase );
  }

  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleBlockedByArmy( game ) ) return false;

    // - check for stopped by magic
    return true;
  }

  private boolean handleBlockedByArmy( Game game )
  {
    // Can't be blocked relocating to your own army.
    if ( _newBase instanceof Army ) return false;

    // Is an opposing army blocking the destination pop center?
    for ( Army army : game.getAllPlayerArmies() )
    {
      boolean isOpponentsArmy = !army.getOwner().equals( _subject.getOwner() );
      boolean isOnThePopCenter = army.getLocation().equals( _newBase.getLocation() );
      if ( isOpponentsArmy && isOnThePopCenter )
      {
        addPlayerEvent( game, _subject, _subject.getName() + " tried to move to " + _newBase.getName() +
                                        ", but was blocked by an army of " + army.getOwnerName() + "." );
        return true;

      }
    }
    return false;
  }

  protected void updateViews( Game game )
  {
    if ( (_newBase instanceof PopCenter) &&
         !_newBase.getOwner().equals( Player.UNOWNED ) &&
         !_newBase.getOwner().equals( _subject.getOwner() ) )
    {
      if ( _r.nextInt( 100 ) < _percentSeen )
      {
        Player owningPlayer = _newBase.getOwner();
        Diplomat diplomatMoveAlert = new Diplomat( GameItem.UNKNOWN_ID, "Unknown", game.getTurn(), _newBase );
        owningPlayer.addKnownItem( diplomatMoveAlert );

        String seenMessage = "A diplomat of " + _subject.getOwnerName() + " was seen moving into " + _newBase.getName();
        addPlayerEvent( game, _newBase, seenMessage );
      }
    }
  }
}
