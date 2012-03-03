package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.Mechanics;
import org.apache.log4j.Logger;

/** Attempt to convince a PopCenter to join the diplomat's kingdom. */
public class DiplomatNegotiateFealty extends AbstractDiplomaticAttempt
{
  private static final Logger LOG = Logger.getLogger( DiplomatNegotiateFealty.class );

  private transient int _diplomatHits;

  public DiplomatNegotiateFealty() { super(); } // GSON only
  public DiplomatNegotiateFealty( Diplomat diplomat )
  {
    super( diplomat );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleAlreadyOwnsPop( game ) ) return false;
    return true;
  }

  public String getShortDescription()
  {
    return _subject.getName() + " negotiate fealty in " + _subject.getBase().getName() + _subject.getLocation().getCoord();
  }

  private boolean handleAlreadyOwnsPop( Game game )
  {
    if ( _subject.getBase().getOwner().equals( _subject.getOwner() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is in a population center already under our control." );
      return true;
    }
    return false;
  }

  protected boolean makeAttempt( Game game )
  {
    _diplomatHits = Mechanics.standardLevelRoll( _subject.getLevel() );
    return _diplomatHits >= determineResistance( game );
  }

  protected void handleAttemptSuccess( Game game )
  {
    PopCenter pop = (PopCenter) _subject.getBase();
    _originalOwner.remove( pop );
    _subject.getOwner().add( pop );
    publishDiplomatOwnerSuccessEvent( game, pop );
    publishPopOwnerSuccessEvent( game, pop );
  }

  private void publishDiplomatOwnerSuccessEvent( Game game, PopCenter pop )
  {
    String seenMessage = _wasSeen ? " with public speeches." : " with well placed anonymous bribes.";
    String resultMessage = _subject.getName() + " successfully convinced " + pop.getName() + " to join our kingdom " + seenMessage;
    addPlayerEvent( game, _subject, resultMessage, pop.getLocation() );
  }

  private void publishPopOwnerSuccessEvent( Game game, PopCenter pop )
  {
    if ( !_originalOwner.equals( _subject.getOwner() ) )
    {
      String seenMessage = _wasSeen ?
                           "Sources say " + _subject.getName() + " was responsible." :
                           "The culprit could not be identified.";
      String resultMessage = pop.getName() + " has rejected our rule and joined " +
                             _subject.getOwner().getName() + ". " + seenMessage;
      addPlayerEvent( game, _originalOwner, resultMessage, pop.getLocation() );
    }
  }

  protected void handleAttemptFailure( Game game )
  {
    _wasMobActivated = mobActivates( game );
    if ( _wasMobActivated )
    {
      _subject.getOwner().remove( _subject );
    }

    publishDiplomatOwnerFailureEvent( game );
    publishPopOwnerFailureEvent( game );
  }

  private void publishDiplomatOwnerFailureEvent( Game game )
  {
    PopCenter pop = (PopCenter) _subject.getBase();
    String seenMessage = _wasSeen ? "made anonymous bribes" : "made public speeches";
    String resultMessage =
      _subject.getName() + " " + seenMessage + ", but failed to convince " + pop.getName() + " to join our kingdom" +
      (_wasMobActivated ? " and was killed by an angry mob." : ".");
    addPlayerEvent( game, _subject, resultMessage, pop.getLocation() );
  }

  private void publishPopOwnerFailureEvent( Game game )
  {
    PopCenter pop = (PopCenter) _subject.getBase();
    if ( !_originalOwner.equals( _subject.getOwner() ) )
    {
      String seenMessage = _wasSeen ?
                           "Sources say " + _subject.getName() + " was responsible for the attempt" :
                           "The culprit could not be identified";
      seenMessage += _wasMobActivated ? " and was killed by an angry mob." : ".";
      String resultMessage = pop.getName() + " rejected an offer to join " + _subject.getOwner().getName() + ". " + seenMessage;
      addPlayerEvent( game, _originalOwner, resultMessage, pop.getLocation() );
    }
  }

  /** @return true if a mob was activated by the diplomatic failure. */
  protected boolean mobActivates( Game game )
  {
    return _diplomatHits <= determineResistance( game ) / 2;
  }

  protected int determinePopResistance( Game game )
  {
    PopCenter popCenter = (PopCenter) _subject.getBase();
    int effectivePopLevel = Math.max( 1, popCenter.getLevel() + getEmbassyImpact( game ) );
    int resistance = popCenter.isOwned() ? effectivePopLevel * 2 : effectivePopLevel;
    return resistance;
  }
}
