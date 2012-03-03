package kessel.hex.orders.diplomat;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.orders.wizard.CharmRegion;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;

/** An abstract class for those bits of functionality that are the same for all diplomatic attempts. */
public abstract class AbstractDiplomaticAttempt extends AbstractDiplomatOrder
{
  private static final Logger LOG = Logger.getLogger( AbstractDiplomaticAttempt.class );

  /** How often the diplomat's action is reported to the affected player. */
  static int PERCENT_SEEN = 50;
  protected final transient Random _r = new Random();

  /** Track some state about the execution of the order. */
  protected transient boolean _wasSuccessful = false;
  protected transient boolean _wasMobActivated = false;
  protected transient boolean _wasSeen = false;
  protected transient Player _originalOwner;

  protected AbstractDiplomaticAttempt() { super(); } // GSON only
  protected AbstractDiplomaticAttempt( Diplomat subject ) { super( subject ); }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleSubjectNotInPop( game ) ) return false;
    return true;
  }

  private boolean handleSubjectNotInPop( Game game )
  {
    if ( !_subject.isInPopCenter() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " must be based in a population center to incite rebellion." );
      return true;
    }
    return false;
  }

  public void processOrder( Game game )
  {
    _wasSuccessful = makeAttempt( game );
    _originalOwner = _subject.getBase().getOwner();
    _wasSeen = _r.nextInt( 100 ) < PERCENT_SEEN;
    if ( _wasSuccessful )
    {
      handleAttemptSuccess( game );
    }
    else
    {
      handleAttemptFailure( game );
    }
  }

  /** @return true if the diplomatic attempt is successful. */
  protected abstract boolean makeAttempt( Game game );

  protected abstract void handleAttemptSuccess( Game game );

  protected abstract void handleAttemptFailure( Game game );

  protected void updateViews( Game game )
  {
    // Report the diplomat if seen in the attempt, well, unless a mob activated in which case the diplomat is dead.
    boolean wasMyOwnPop = _originalOwner.equals( _subject.getOwner() );
    if ( !wasMyOwnPop && _wasSeen && !_wasMobActivated )
    {
      Diplomat diplomatAlert = new Diplomat( _subject );
      diplomatAlert.setTurnSeen( game.getTurn() );
      _originalOwner.addKnownItem( diplomatAlert );
    }

    // Even if the pop was lost, the previous owner still knows about it.
    if ( _wasSuccessful )
    {
      PopCenter intelPop = new PopCenter( (PopCenter) _subject.getBase() );
      _originalOwner.addKnownItem( intelPop );
    }
  }

  /** @return the amount of resistance that must be met to succeed. */
  public int determineResistance( Game game )
  {
    int popResistance = determinePopResistance( game );
    int inspiredLoyalty = determineLoyaltyResistance( game );
    if ( LOG.isDebugEnabled() )
    { LOG.debug( _subject.getBase().getName() + " resistance of P" + popResistance + " + I" + inspiredLoyalty ); }
    return popResistance + inspiredLoyalty;
  }

  protected abstract int determinePopResistance( Game game );

  @SuppressWarnings({ "ClassReferencesSubclass" })
  /** @return the amount of resistance due to inspired loyalty. Only the best such order applies (i.e. they aren't cumulative). */
  protected int determineLoyaltyResistance( Game game )
  {
    PopCenter popCenter = (PopCenter) _subject.getBase();
    List<DiplomatInspireLoyalty> loyaltyOrders = game.getCurrentTurn().getOrdersOfType( DiplomatInspireLoyalty.class );
    int inspiredLoyalty = 0;
    for ( DiplomatInspireLoyalty loyaltyOrder : loyaltyOrders )
    {
      boolean affectsThisOrder = loyaltyOrder._subject.getBase().equals( popCenter );
      if ( affectsThisOrder )
      {
        inspiredLoyalty = Math.max( inspiredLoyalty, loyaltyOrder._subject.getLevel() );
      }
    }
    return inspiredLoyalty;
  }

  public boolean wasSuccessful() { return _wasSuccessful; }

  /**
   * Return the impact of embassies in the region. For example, if the diplomat has 1 level and the pop owner has 2 levels, then the
   * impact is +1, meaning it's harder for the diplomat to be successful.
   *
   * @return the impact of embassies in the region.
   */
  protected int getEmbassyImpact( Game game )
  {
    Region region = _subject.getBase().getLocation().getRegion();
    int diplomatEmbassyLevel = _subject.getOwner().getEmbassyLevel( region );
    diplomatEmbassyLevel += getCharmImpact( _subject, game );

    int popOwnerEmbassyLevel = 0;
    if ( _subject.getBase().isOwned() )
    {
      popOwnerEmbassyLevel = _subject.getBase().getOwner().getEmbassyLevel( region );
      popOwnerEmbassyLevel += getCharmImpact( _subject.getBase(), game );
    }
    return popOwnerEmbassyLevel - diplomatEmbassyLevel;
  }

  protected int getCharmImpact( GameItem subject, Game game )
  {
    List<CharmRegion> charmOrders = game.getCurrentTurn().getOrdersOfType( CharmRegion.class );
    int charmLevel = 0;
    for ( CharmRegion charmOrder : charmOrders )
    {
      // Is the charm order by the subject's owner for the subject's region?
      if ( charmOrder.getSubject().getOwner().equals( subject.getOwner() ) &&
           charmOrder.getSubject().getLocation().getRegion().equals( subject.getLocation().getRegion() ) )
      {
        charmLevel = Math.max( charmLevel, charmOrder.getCharmLevel() );
      }
    }
    return charmLevel;
  }
}
