package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.Mechanics;

/** Attempt to convince a PopCenter to rebel against it's current owner. */
public class DiplomatInciteRebellion extends AbstractDiplomaticAttempt
{
  private int _diplomatHits;

  public DiplomatInciteRebellion() { super(); } // GSON only
  public DiplomatInciteRebellion( Diplomat diplomat )
  {
    super( diplomat );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handlePopNotOwned( game ) ) return false;
    if ( handleInciteInOwnCapitol( game ) ) return false;
    return true;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " incite rebellion in " + _subject.getBase().getName() + _subject.getLocation().getCoord();
  }

  private boolean handlePopNotOwned( Game game )
  {
    if ( !_subject.getBase().isOwned() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is in a population center that was already neutral." );
      return true;
    }
    return false;
  }

  private boolean handleInciteInOwnCapitol( Game game )
  {
    // You can't incite rebellion in your own capitol.
    if ( _subject.getOwner().getCapitol().equals( _subject.getBase() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " can't incite rebellion in the kingdom's own capital." );
      return true;
    }
    return false;
  }

  protected boolean makeAttempt( Game game )
  {
    // Inciting in your own pop center always works.
    PopCenter pop = (PopCenter) _subject.getBase();
    if ( _subject.getOwner().equals( pop.getOwner() ) )
    {
      return true;
    }
    // Inciting in other capitols always fails.
    else if ( pop.isCapitol() )
    {
      return false;
    }
    else
    {
      _diplomatHits = Mechanics.standardLevelRoll( _subject.getLevel() );
      return _diplomatHits >= determineResistance( game );
    }
  }

  protected void handleAttemptSuccess( Game game )
  {
    PopCenter pop = (PopCenter) _subject.getBase();
    _originalOwner.remove( pop );
    pop.setOwner( Player.UNOWNED );
    publishDiplomatOwnerSuccessEvent( game, pop );
    publishPopOwnerSuccessEvent( game, pop );
  }

  private void publishPopOwnerSuccessEvent( Game game, PopCenter pop )
  {
    if ( !_originalOwner.equals( _subject.getOwner() ) )
    {
      String seenMessage =
        _wasSeen ?
        " Sources say " + _subject.getName() + " of " + _subject.getOwner().getName() + " was responsible." :
        " The culprit could not be identified.";
      String resultMessage = pop.getName() + " has gone into rebellion." + seenMessage;
      addPlayerEvent( game, _originalOwner, resultMessage, pop.getLocation() );
    }
  }

  private void publishDiplomatOwnerSuccessEvent( Game game, PopCenter pop )
  {
    String seenMessage = _wasSeen ? " with public speeches." : " with well placed anonymous bribes.";
    String resultMessage = _subject.getName() + " successfully incited rebellion in " + pop.getName() + seenMessage;
    addPlayerEvent( game, _subject, resultMessage, pop.getLocation() );
  }

  protected void handleAttemptFailure( Game game )
  {
    PopCenter pop = (PopCenter) _subject.getBase();
    _wasMobActivated = mobActivates( game );
    if ( _wasMobActivated )
    {
      _subject.getOwner().remove( _subject );
    }

    publishDiplomatOwnerFailureEvent( game, pop );
    publishPopOwnerFailureEvent( game, pop );
  }

  private void publishPopOwnerFailureEvent( Game game, PopCenter pop )
  {
    if ( !_originalOwner.equals( _subject.getOwner() ) )
    {
      String seenMessage =
        _wasSeen ?
        " Sources say " + _subject.getName() + " of " + _subject.getOwner().getName() + " was responsible" :
        " The culprit could not be identified ";
      seenMessage += _wasMobActivated ? " and was killed by an angry mob." : ".";
      String resultMessage = pop.getName() + " resisted an attempt to incite rebellion." + seenMessage;
      addPlayerEvent( game, _originalOwner, resultMessage, pop.getLocation() );
    }
  }

  private void publishDiplomatOwnerFailureEvent( Game game, PopCenter pop )
  {
    String seenMessage = _wasSeen ? "made anonymous bribes" : "made public speeches";
    String resultMessage =
      _subject.getName() + " " + seenMessage + ", but failed to incited rebellion in " + pop.getName() +
      (_wasMobActivated ? " and was killed by an angry mob." : ".");
    addPlayerEvent( game, _subject, resultMessage, pop.getLocation() );
  }

  protected boolean mobActivates( Game game )
  {
    return _diplomatHits <= determineResistance( game ) / 2;
  }

  protected int determinePopResistance( Game game )
  {
    PopCenter popCenter = (PopCenter) _subject.getBase();
    int effectivePopLevel = Math.max( 1, popCenter.getLevel() + getEmbassyImpact( game ) );
    return effectivePopLevel;
  }
}
